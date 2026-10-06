package com.forehapp.store.supplierSyncModule.application.usecases;

import com.forehapp.store.notificationModule.domain.ports.in.NotificationUseCase;
import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.storeModule.domain.ports.out.IStoreDao;
import com.forehapp.store.supplierSyncModule.application.SupplierSyncSettings;
import com.forehapp.store.supplierSyncModule.application.dto.IngestResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SupplierCatalogIngestDto;
import com.forehapp.store.supplierSyncModule.application.dto.SyncRunResponse;
import com.forehapp.store.supplierSyncModule.application.dto.VariantForMatchingResponse;
import com.forehapp.store.supplierSyncModule.application.usecases.SupplierSyncWriter.RunOutcome;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncConfig;
import com.forehapp.store.supplierSyncModule.domain.model.VariantRow;
import com.forehapp.store.supplierSyncModule.domain.ports.in.ISupplierSyncIngestService;
import com.forehapp.store.supplierSyncModule.domain.ports.out.ISupplierSyncDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SupplierSyncIngestServiceImpl implements ISupplierSyncIngestService {

    private static final Logger log = LoggerFactory.getLogger(SupplierSyncIngestServiceImpl.class);
    private static final ZoneId STORE_ZONE = ZoneId.of("America/Bogota");

    private final ISupplierSyncDao dao;
    private final SupplierSyncWriter writer;
    private final SupplierSyncEmailBuilder emailBuilder;
    private final NotificationUseCase notificationUseCase;
    private final IStoreDao storeDao;
    private final SupplierSyncSettings settings;

    public SupplierSyncIngestServiceImpl(ISupplierSyncDao dao,
                                         SupplierSyncWriter writer,
                                         SupplierSyncEmailBuilder emailBuilder,
                                         NotificationUseCase notificationUseCase,
                                         IStoreDao storeDao,
                                         SupplierSyncSettings settings) {
        this.dao = dao;
        this.writer = writer;
        this.emailBuilder = emailBuilder;
        this.notificationUseCase = notificationUseCase;
        this.storeDao = storeDao;
        this.settings = settings;
    }

    @Override
    public IngestResponse ingest(SupplierCode supplier, SupplierCatalogIngestDto dto) {
        // MySQL DATETIME drops fractions of a second; a truncated run time keeps "seen in this run" comparable
        LocalDateTime runTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Map<String, SupplierCatalogIngestDto.Item> itemsByKey = new LinkedHashMap<>();
        for (SupplierCatalogIngestDto.Item item : dto.items()) {
            String key = SupplierNames.normalize(item.name());
            if (!key.isEmpty()) itemsByKey.put(key, item);
        }
        int outOfStock = (int) itemsByKey.values().stream().filter(SupplierCatalogIngestDto.Item::outOfStock).count();

        String abortReason = checkCatalog(supplier, itemsByKey.size(), outOfStock);
        List<SupplierSyncConfig> configs = dao.findEnabledConfigs(supplier);

        int suggestionsStored = 0;
        if (abortReason == null) {
            writer.upsertCatalog(supplier, itemsByKey, runTime);
            Set<Long> storeIds = configs.stream().map(SupplierSyncConfig::getStoreId).collect(Collectors.toSet());
            suggestionsStored = writer.storeSuggestions(supplier, dto.suggestions(), storeIds);
        } else {
            log.warn("[SupplierSync] {} catalog rejected: {}", supplier, abortReason);
        }

        List<RunOutcome> outcomes = new ArrayList<>();
        for (SupplierSyncConfig config : configs) {
            try {
                outcomes.add(writer.runStore(config, runTime, abortReason, itemsByKey.size(), outOfStock));
            } catch (Exception e) {
                log.error("[SupplierSync] Run failed for storeId={} supplier={}", config.getStoreId(), supplier, e);
            }
        }
        notify(outcomes);

        log.info("[SupplierSync] {} ingest: items={} outOfStock={} suggestions={} runs={}",
                supplier, itemsByKey.size(), outOfStock, suggestionsStored, outcomes.size());
        return new IngestResponse(itemsByKey.size(), outOfStock, suggestionsStored, abortReason,
                outcomes.stream().map(o -> SyncRunResponse.from(o.run())).toList());
    }

    @Override
    public List<VariantForMatchingResponse> variantsForMatching(SupplierCode supplier) {
        Set<Long> storeIds = dao.findEnabledConfigs(supplier).stream()
                .map(SupplierSyncConfig::getStoreId).collect(Collectors.toSet());
        List<VariantRow> variants = dao.findVariantsWithoutLink(storeIds, supplier);
        Map<Long, List<String[]>> attributes = dao.findVariantAttributes(
                variants.stream().map(VariantRow::variantId).toList());

        return variants.stream().map(v -> new VariantForMatchingResponse(
                v.variantId(), v.productId(), v.storeId(), v.productTitle(), v.brand(), v.sku(), v.price(),
                Boolean.TRUE.equals(v.active()),
                attributes.getOrDefault(v.variantId(), List.of()).stream()
                        .map(a -> new VariantForMatchingResponse.Attribute(a[0], a[1]))
                        .toList()
        )).toList();
    }

    /** Rejects a scrape that looks broken (empty, much smaller than usual, or almost all out of stock). */
    private String checkCatalog(SupplierCode supplier, int items, int outOfStock) {
        if (items == 0) {
            return "The supplier catalog arrived empty.";
        }
        Optional<Integer> previous = dao.findLastAcceptedCatalogSize(supplier);
        if (previous.isPresent() && previous.get() > 0 && items < previous.get() * settings.getMinCatalogRatio()) {
            return "The supplier catalog has " + items + " products, the previous run had " + previous.get() + ".";
        }
        if ((double) outOfStock / items > settings.getMaxOutOfStockRatio()) {
            return outOfStock + " of " + items + " supplier products are marked out of stock.";
        }
        return null;
    }

    private void notify(List<RunOutcome> outcomes) {
        if (outcomes.isEmpty() || settings.getNotifyEmails().isEmpty()) return;

        Map<Long, String> storeNames = new HashMap<>();
        for (RunOutcome outcome : outcomes) {
            Long storeId = outcome.run().getStoreId();
            storeNames.put(storeId, storeDao.findById(storeId).map(Store::getName).orElse("Tienda #" + storeId));
        }
        String subject = emailBuilder.buildSubject(outcomes, LocalDate.now(STORE_ZONE));
        String html = emailBuilder.buildHtml(outcomes, storeNames);
        for (String email : settings.getNotifyEmails()) {
            notificationUseCase.sendEmailNotification(email, subject, html);
        }
    }
}
