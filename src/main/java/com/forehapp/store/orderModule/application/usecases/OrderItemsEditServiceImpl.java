package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.ambassadorModule.domain.model.AmbassadorCommission;
import com.forehapp.store.ambassadorModule.domain.model.CommissionStatus;
import com.forehapp.store.ambassadorModule.domain.ports.out.ICommissionDao;
import com.forehapp.store.donationModule.domain.model.DonationRecord;
import com.forehapp.store.donationModule.domain.model.DonationRecordStatus;
import com.forehapp.store.donationModule.domain.ports.out.IDonationRecordDao;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.orderModule.domain.events.OrderItemsChangedEvent;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderItemChange;
import com.forehapp.store.orderModule.domain.model.OrderItemChangeType;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.domain.model.OrderStatus;
import com.forehapp.store.orderModule.domain.ports.in.IOrderItemsEditService;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.orderModule.domain.ports.out.IOrderGroupDao;
import com.forehapp.store.orderModule.domain.ports.out.IOrderItemChangeDao;
import com.forehapp.store.orderModule.infrastructure.web.dto.EditOrderItemsRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.EditOrderItemsResponseDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderItemChangeDto;
import com.forehapp.store.paymentModule.application.usecases.MercadoPagoService;
import com.forehapp.store.paymentModule.domain.model.Payment;
import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import com.forehapp.store.paymentModule.domain.model.PaymentStatus;
import com.forehapp.store.paymentModule.infrastructure.persistence.IPaymentRepository;
import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.model.ProductVariant;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.productModule.domain.ports.out.IProductVariantDao;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Seller edits the products of one seller group before it ships. The request is the list the group
 * should end with; the difference is applied in one go:
 * - stock: own units of what leaves go back to stock, what comes in is taken own stock first, then dropship
 * - totals: group subtotal and order total follow the new lines; shipping and coupon discount stay as they were
 *   (a Mercado Pago surcharge follows the new total)
 * - payment: unpaid orders get the new amount (Mercado Pago: a new link); paid ones keep a balance to settle by hand
 * - pending ambassador commission and donation follow the new amounts
 * - one history entry per changed line and one email to the buyer
 */
@Service
public class OrderItemsEditServiceImpl implements IOrderItemsEditService {

    private static final List<String> CATALOG_CACHES = List.of(
            "public-products", "public-product-brand-facets", "discovery-sections",
            "seller-products", "seller-product-detail", "wishlist");

    private final IOrderGroupDao orderGroupDao;
    private final IOrderDao orderDao;
    private final IOrderItemChangeDao changeDao;
    private final IProductVariantDao variantDao;
    private final IProductDao productDao;
    private final IStoreMembershipDao membershipDao;
    private final IPaymentRepository paymentRepository;
    private final MercadoPagoService mercadoPagoService;
    private final ICommissionDao commissionDao;
    private final IDonationRecordDao donationRecordDao;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.payment.mercado-pago-surcharge-rate:0.03}")
    private BigDecimal mercadoPagoSurchargeRate;

    public OrderItemsEditServiceImpl(IOrderGroupDao orderGroupDao,
                                     IOrderDao orderDao,
                                     IOrderItemChangeDao changeDao,
                                     IProductVariantDao variantDao,
                                     IProductDao productDao,
                                     IStoreMembershipDao membershipDao,
                                     IPaymentRepository paymentRepository,
                                     MercadoPagoService mercadoPagoService,
                                     ICommissionDao commissionDao,
                                     IDonationRecordDao donationRecordDao,
                                     CacheManager cacheManager,
                                     ApplicationEventPublisher eventPublisher) {
        this.orderGroupDao = orderGroupDao;
        this.orderDao = orderDao;
        this.changeDao = changeDao;
        this.variantDao = variantDao;
        this.productDao = productDao;
        this.membershipDao = membershipDao;
        this.paymentRepository = paymentRepository;
        this.mercadoPagoService = mercadoPagoService;
        this.commissionDao = commissionDao;
        this.donationRecordDao = donationRecordDao;
        this.cacheManager = cacheManager;
        this.eventPublisher = eventPublisher;
    }

    /** One planned change: item is null for ADDED, line is null for REMOVED. */
    private record Planned(OrderItemChangeType type, OrderItem item, EditOrderItemsRequestDto.Line line,
                           Long oldVariantId, String oldLabel, Integer oldQuantity, BigDecimal oldUnitPrice) {
        boolean movesStock() {
            return type != OrderItemChangeType.UPDATED || !line.quantity().equals(oldQuantity);
        }
    }

    @Override
    @Transactional
    public EditOrderItemsResponseDto editItems(Long storeId, Long groupId, EditOrderItemsRequestDto dto, Long userId) {
        requireManager(storeId, userId);
        OrderSellerGroup group = resolveGroup(groupId, storeId);
        Order order = group.getOrder();

        if (order.getStatus() == OrderStatus.CANCELLED
                || (group.getStatus() != OrderSellerGroupStatus.PENDING && group.getStatus() != OrderSellerGroupStatus.PREPARING)) {
            throw new ConflictException(ErrorCode.ORDER_GROUP_INVALID_STATUS,
                    "Products can only be changed before the order ships");
        }

        List<Planned> plan = plan(group, dto);
        if (plan.isEmpty()) {
            throw new BadRequestException(ErrorCode.ORDER_EDIT_NO_CHANGES, "The request does not change any product");
        }

        Map<Long, ProductVariant> variants = lockVariants(group, plan);
        for (Planned p : plan) {
            if (p.type() == OrderItemChangeType.REMOVED) continue;
            ProductVariant v = variants.get(p.line().variantId());
            if (v == null || !v.getProduct().getStore().getId().equals(storeId)) {
                throw new BadRequestException(ErrorCode.ORDER_EDIT_VARIANT_INVALID,
                        "Product variant " + p.line().variantId() + " does not belong to this store");
            }
            boolean newVariant = p.item() == null || !p.item().getVariant().getId().equals(v.getId());
            if (newVariant && !Boolean.TRUE.equals(v.getActive())) {
                throw new BadRequestException(ErrorCode.ORDER_EDIT_VARIANT_INVALID,
                        "Product variant " + v.getId() + " is not active");
            }
        }

        // Stock: give back what leaves before taking what comes in, so lines can trade units between them
        for (Planned p : plan) {
            if (p.item() != null && p.movesStock()) {
                OrderItem item = p.item();
                ProductVariant old = variants.get(p.oldVariantId());
                int dropship = item.getDropshipQuantity() == null ? 0 : item.getDropshipQuantity();
                old.setStock(old.getStock() + Math.max(item.getQuantity() - dropship, 0));
            }
        }
        // Identity map: two added lines can be equal as values
        Map<Planned, Integer> dropshipByLine = new IdentityHashMap<>();
        for (Planned p : plan) {
            if (p.type() == OrderItemChangeType.REMOVED || !p.movesStock()) continue;
            ProductVariant v = variants.get(p.line().variantId());
            if (!v.canFulfill(p.line().quantity())) {
                throw new ConflictException(ErrorCode.ORDER_INSUFFICIENT_STOCK,
                        "Insufficient stock for: " + v.getProduct().getTitle());
            }
            dropshipByLine.put(p, v.consume(p.line().quantity()));
        }
        variants.values().forEach(variantDao::save);

        // Apply to the order lines
        String editId = UUID.randomUUID().toString();
        BigDecimal subtotalBefore = group.getSubtotal();
        BigDecimal totalBefore = order.getTotal();
        List<OrderItemChange> history = new ArrayList<>();
        List<OrderItemsChangedEvent.Line> emailLines = new ArrayList<>();

        for (Planned p : plan) {
            OrderItem item = p.item();
            switch (p.type()) {
                case REMOVED -> group.getItems().remove(item);
                case ADDED -> {
                    item = new OrderItem();
                    item.setSellerGroup(group);
                    group.getItems().add(item);
                }
                default -> { }
            }
            String newLabel = null;
            if (p.type() != OrderItemChangeType.REMOVED) {
                ProductVariant v = variants.get(p.line().variantId());
                if (!v.getId().equals(item.getVariant() == null ? null : item.getVariant().getId())) {
                    item.setVariant(v);
                    item.setUnitCost(v.getCost());
                }
                item.setQuantity(p.line().quantity());
                item.setUnitPrice(p.line().unitPrice());
                if (dropshipByLine.containsKey(p)) item.setDropshipQuantity(dropshipByLine.get(p));
                newLabel = label(v);
            }
            history.add(change(editId, order, group, p, newLabel, dto.reason().trim(), userId));
            emailLines.add(new OrderItemsChangedEvent.Line(p.type().name(), p.oldLabel(), p.oldQuantity(), p.oldUnitPrice(),
                    newLabel, p.line() == null ? null : p.line().quantity(), p.line() == null ? null : p.line().unitPrice()));
        }

        // Totals
        BigDecimal subtotal = group.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal delta = subtotal.subtract(subtotalBefore);
        group.setSubtotal(subtotal);
        group.setItemsEditedAt(LocalDateTime.now());
        order.setTotal(newOrderTotal(order, delta));
        BigDecimal totalDelta = order.getTotal().subtract(totalBefore);

        // Payment
        boolean paid = order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PAYMENT_CONFIRMED;
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        String paymentUrl = null;
        if (paid) {
            BigDecimal balance = (order.getBalanceDue() == null ? BigDecimal.ZERO : order.getBalanceDue()).add(totalDelta);
            order.setBalanceDue(balance.signum() == 0 ? null : balance);
        }

        orderGroupDao.save(group);
        orderDao.save(order);

        if (!paid && payment != null && PaymentStatus.PENDING.name().equals(payment.getStatus())
                && totalDelta.signum() != 0) {
            if (PaymentMethod.MERCADO_PAGO.name().equals(order.getPaymentMethod())) {
                paymentUrl = mercadoPagoService.refreshPreference(order, payment);
            } else {
                payment.setAmount(order.getTotal());
                paymentRepository.save(payment);
            }
        }

        updateCommissions(order);
        updateDonations(order.getId(), delta);
        refreshProducts(variants.values());
        changeDao.saveAll(history.stream().peek(h -> h.setOrderTotalAfter(order.getTotal())).toList());

        eventPublisher.publishEvent(new OrderItemsChangedEvent(
                order.getId(), order.getBuyerEmail(), order.resolveContactName(), group.getStore().getName(),
                dto.reason().trim(), emailLines, totalBefore, order.getTotal(),
                !paid, paid ? order.getBalanceDue() : null, paymentUrl));

        return new EditOrderItemsResponseDto(order.getId(), group.getId(), subtotal, totalBefore, order.getTotal(),
                order.getBalanceDue(), paymentUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemChangeDto> getChanges(Long storeId, Long groupId, Long userId) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED,
                        "You are not an active member of this store"));
        resolveGroup(groupId, storeId);
        return changeDao.findByGroupId(groupId).stream().map(OrderItemChangeDto::from).toList();
    }

    @Override
    @Transactional
    public void settleBalance(Long storeId, Long groupId, Long userId) {
        requireManager(storeId, userId);
        OrderSellerGroup group = resolveGroup(groupId, storeId);
        Order order = group.getOrder();
        if (order.getBalanceDue() == null) {
            throw new ConflictException(ErrorCode.ORDER_BALANCE_NOT_DUE, "This order has no pending difference");
        }
        // The balance covers the whole order: only a store that owns all of it can settle it
        boolean otherStores = orderGroupDao.findAllByOrderId(order.getId()).stream()
                .anyMatch(g -> !g.getStore().getId().equals(storeId));
        if (otherStores) {
            throw new ConflictException(ErrorCode.ORDER_PAYMENT_OTHER_STORES,
                    "The order includes other stores; its balance must be settled by an admin");
        }
        order.setBalanceDue(null);
        orderDao.save(order);
    }

    // ── Planning ────────────────────────────────────────────────────────────────

    private List<Planned> plan(OrderSellerGroup group, EditOrderItemsRequestDto dto) {
        Map<Long, OrderItem> current = group.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, i -> i));
        Set<Long> kept = new HashSet<>();
        List<Planned> plan = new ArrayList<>();

        for (EditOrderItemsRequestDto.Line line : dto.items()) {
            if (line.itemId() == null) {
                plan.add(new Planned(OrderItemChangeType.ADDED, null, line, null, null, null, null));
                continue;
            }
            OrderItem item = current.get(line.itemId());
            if (item == null || !kept.add(line.itemId())) {
                throw new BadRequestException(ErrorCode.ORDER_EDIT_ITEM_NOT_FOUND,
                        "Item " + line.itemId() + " is not part of this order group (or is repeated)");
            }
            boolean sameVariant = item.getVariant().getId().equals(line.variantId());
            boolean same = sameVariant && item.getQuantity().equals(line.quantity())
                    && item.getUnitPrice().compareTo(line.unitPrice()) == 0;
            if (same) continue;
            plan.add(new Planned(sameVariant ? OrderItemChangeType.UPDATED : OrderItemChangeType.REPLACED,
                    item, line, item.getVariant().getId(), label(item.getVariant()), item.getQuantity(), item.getUnitPrice()));
        }
        for (OrderItem item : group.getItems()) {
            if (!kept.contains(item.getId())) {
                plan.add(new Planned(OrderItemChangeType.REMOVED, item, null, item.getVariant().getId(),
                        label(item.getVariant()), item.getQuantity(), item.getUnitPrice()));
            }
        }
        return plan;
    }

    /** Locks every variant involved, in id order, so two edits or a checkout cannot interleave. */
    private Map<Long, ProductVariant> lockVariants(OrderSellerGroup group, List<Planned> plan) {
        Set<Long> ids = new TreeSet<>();
        for (Planned p : plan) {
            if (p.oldVariantId() != null) ids.add(p.oldVariantId());
            if (p.line() != null) ids.add(p.line().variantId());
        }
        Map<Long, ProductVariant> variants = new HashMap<>();
        for (Long id : ids) {
            variantDao.findByIdForUpdate(id).ifPresent(v -> variants.put(id, v));
        }
        return variants;
    }

    // ── Totals, commissions, donations ──────────────────────────────────────────

    /** Shipping and coupon discount stay as they were; a Mercado Pago surcharge is recomputed on the new base. */
    private BigDecimal newOrderTotal(Order order, BigDecimal subtotalDelta) {
        BigDecimal surcharge = order.getMercadoPagoSurcharge();
        if (surcharge == null || !PaymentMethod.MERCADO_PAGO.name().equals(order.getPaymentMethod())) {
            return order.getTotal().add(subtotalDelta).max(BigDecimal.ZERO);
        }
        BigDecimal base = order.getTotal().subtract(surcharge).add(subtotalDelta).max(BigDecimal.ZERO);
        BigDecimal newSurcharge = base.multiply(mercadoPagoSurchargeRate).setScale(2, RoundingMode.HALF_UP);
        order.setMercadoPagoSurcharge(newSurcharge);
        return base.add(newSurcharge);
    }

    /** Same profit formula as checkout, on the order's current items. Paid-out commissions are left alone. */
    private void updateCommissions(Order order) {
        List<AmbassadorCommission> pending = commissionDao.findByOrderId(order.getId()).stream()
                .filter(c -> c.getStatus() == CommissionStatus.PENDING)
                .toList();
        if (pending.isEmpty()) return;

        BigDecimal revenue = BigDecimal.ZERO;
        for (OrderSellerGroup g : order.getSellerGroups()) {
            for (OrderItem i : g.getItems()) {
                revenue = revenue.add(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
            }
        }
        BigDecimal discount = order.getCouponDiscount();
        BigDecimal discountRatio = discount != null && discount.signum() > 0 && revenue.signum() > 0
                ? discount.divide(revenue, 10, RoundingMode.HALF_UP).min(BigDecimal.ONE)
                : BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
        for (OrderSellerGroup g : order.getSellerGroups()) {
            for (OrderItem i : g.getItems()) {
                if (i.getUnitCost() == null) continue;
                BigDecimal itemRevenue = i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
                        .multiply(BigDecimal.ONE.subtract(discountRatio));
                BigDecimal itemCost = i.getUnitCost().multiply(BigDecimal.valueOf(i.getQuantity()));
                profit = profit.add(itemRevenue.subtract(itemCost).max(BigDecimal.ZERO));
            }
        }
        for (AmbassadorCommission c : pending) {
            c.setCommissionAmount(profit.multiply(c.getCommissionPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            commissionDao.save(c);
        }
    }

    /** A donation is a percentage of the products amount, so it moves by that percentage of the change. */
    private void updateDonations(Long orderId, BigDecimal subtotalDelta) {
        if (subtotalDelta.signum() == 0) return;
        for (DonationRecord r : donationRecordDao.findByOrderId(orderId)) {
            if (r.getStatus() != DonationRecordStatus.PENDING || r.getDonationPercentage() == null) continue;
            BigDecimal change = subtotalDelta.multiply(r.getDonationPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            r.setDonationAmount(r.getDonationAmount().add(change).max(BigDecimal.ZERO));
            donationRecordDao.save(r);
        }
    }

    private void refreshProducts(Iterable<ProductVariant> variants) {
        Set<Long> productIds = new HashSet<>();
        variants.forEach(v -> productIds.add(v.getProduct().getId()));
        for (Long productId : productIds) {
            Product product = productDao.findById(productId).orElse(null);
            if (product != null && product.refreshStockStatus()) productDao.save(product);
        }
        for (String name : CATALOG_CACHES) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private static String label(ProductVariant v) {
        String attributes = v.getAttributeValues().stream()
                .map(av -> av.getDescription())
                .collect(Collectors.joining(" · "));
        String label = v.getProduct().getTitle() + (attributes.isEmpty() ? "" : " (" + attributes + ")");
        return label.length() <= 400 ? label : label.substring(0, 400);
    }

    private static OrderItemChange change(String editId, Order order, OrderSellerGroup group, Planned p,
                                          String newLabel, String reason, Long userId) {
        OrderItemChange c = new OrderItemChange();
        c.setEditId(editId);
        c.setOrderId(order.getId());
        c.setGroupId(group.getId());
        c.setType(p.type());
        c.setOldVariantId(p.oldVariantId());
        c.setOldLabel(p.oldLabel());
        c.setOldQuantity(p.oldQuantity());
        c.setOldUnitPrice(p.oldUnitPrice());
        if (p.line() != null) {
            c.setNewVariantId(p.line().variantId());
            c.setNewLabel(newLabel);
            c.setNewQuantity(p.line().quantity());
            c.setNewUnitPrice(p.line().unitPrice());
        }
        c.setReason(reason.length() <= 500 ? reason : reason.substring(0, 500));
        c.setOrderTotalBefore(order.getTotal());
        c.setOrderTotalAfter(order.getTotal());
        c.setChangedByUserId(userId);
        return c;
    }

    private void requireManager(Long storeId, Long userId) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .filter(m -> m.getRole() != StoreMemberRole.STAFF)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED,
                        "Only the store's OWNER or MANAGER can change the products of an order"));
    }

    private OrderSellerGroup resolveGroup(Long groupId, Long storeId) {
        OrderSellerGroup group = orderGroupDao.findByIdWithDetails(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_GROUP_NOT_FOUND, "Order group not found"));
        if (!group.getStore().getId().equals(storeId)) {
            throw new ForbiddenException(ErrorCode.ORDER_GROUP_ACCESS_DENIED, "Access denied to this order group");
        }
        return group;
    }
}
