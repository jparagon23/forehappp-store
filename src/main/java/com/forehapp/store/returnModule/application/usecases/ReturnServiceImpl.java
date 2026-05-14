package com.forehapp.store.returnModule.application.usecases;

import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.domain.ports.out.IOrderGroupDao;
import com.forehapp.store.returnModule.application.dto.CreateReturnRequestDto;
import com.forehapp.store.returnModule.application.dto.ReturnItemRequestDto;
import com.forehapp.store.returnModule.application.dto.ReturnResponse;
import com.forehapp.store.returnModule.domain.model.ReturnItem;
import com.forehapp.store.returnModule.domain.model.ReturnRequest;
import com.forehapp.store.returnModule.domain.ports.in.IReturnService;
import com.forehapp.store.returnModule.domain.ports.out.IReturnDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReturnServiceImpl implements IReturnService {

    private final IReturnDao returnDao;
    private final IOrderGroupDao orderGroupDao;
    private final IStoreProfileDao storeProfileDao;

    public ReturnServiceImpl(IReturnDao returnDao,
                             IOrderGroupDao orderGroupDao,
                             IStoreProfileDao storeProfileDao) {
        this.returnDao = returnDao;
        this.orderGroupDao = orderGroupDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public ReturnResponse createReturn(Long userId, CreateReturnRequestDto dto) {
        StoreProfile buyer = resolveProfile(userId);

        OrderSellerGroup group = orderGroupDao.findByIdWithDetails(dto.orderGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order group not found"));

        if (!group.getOrder().getBuyer().getId().equals(buyer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order does not belong to you");
        }
        if (group.getStatus() != OrderSellerGroupStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Returns can only be requested for delivered orders");
        }
        returnDao.findByGroupId(dto.orderGroupId()).ifPresent(r -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A return request already exists for this order group");
        });

        Map<Long, OrderItem> itemsById = group.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, i -> i));

        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderGroup(group);
        returnRequest.setBuyer(buyer);
        returnRequest.setReturnType(dto.returnType());
        returnRequest.setReason(dto.reason());

        for (ReturnItemRequestDto itemDto : dto.items()) {
            OrderItem orderItem = itemsById.get(itemDto.orderItemId());
            if (orderItem == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Item " + itemDto.orderItemId() + " does not belong to this order group");
            }
            if (itemDto.quantityToReturn() > orderItem.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot return more than the ordered quantity for item " + itemDto.orderItemId());
            }
            ReturnItem returnItem = new ReturnItem();
            returnItem.setReturnRequest(returnRequest);
            returnItem.setOrderItem(orderItem);
            returnItem.setQuantityToReturn(itemDto.quantityToReturn());
            returnRequest.getItems().add(returnItem);
        }

        return toResponse(returnDao.save(returnRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnResponse> getMyReturns(Long userId) {
        StoreProfile buyer = resolveProfile(userId);
        return returnDao.findByBuyerId(buyer.getId()).stream().map(ReturnServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnResponse getReturn(Long userId, Long returnId) {
        StoreProfile buyer = resolveProfile(userId);
        ReturnRequest returnRequest = returnDao.findById(returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return request not found"));
        if (!returnRequest.getBuyer().getId().equals(buyer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This return does not belong to you");
        }
        return toResponse(returnRequest);
    }

    private StoreProfile resolveProfile(Long userId) {
        return storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
    }

    static ReturnResponse toResponse(ReturnRequest r) {
        String buyerName = r.getBuyer().getUser().getName() + " " + r.getBuyer().getUser().getLastname();
        List<com.forehapp.store.returnModule.application.dto.ReturnItemResponse> items = r.getItems().stream()
                .map(i -> {
                    OrderItem oi = i.getOrderItem();
                    java.math.BigDecimal subtotal = oi.getUnitPrice()
                            .multiply(java.math.BigDecimal.valueOf(i.getQuantityToReturn()));
                    return new com.forehapp.store.returnModule.application.dto.ReturnItemResponse(
                            oi.getId(),
                            oi.getVariant().getProduct().getTitle(),
                            oi.getVariant().getSku(),
                            oi.getQuantity(),
                            i.getQuantityToReturn(),
                            oi.getUnitPrice(),
                            subtotal
                    );
                }).toList();

        return new ReturnResponse(
                r.getId(),
                r.getOrderGroup().getId(),
                r.getOrderGroup().getOrder().getId(),
                buyerName.trim(),
                r.getReturnType().name(),
                r.getReason(),
                r.getRefundAmount(),
                r.getAdminNotes(),
                r.getStatus().name(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                items
        );
    }
}
