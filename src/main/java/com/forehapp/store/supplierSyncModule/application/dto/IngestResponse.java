package com.forehapp.store.supplierSyncModule.application.dto;

import java.util.List;

public record IngestResponse(int items, int outOfStock, int suggestionsStored, String abortReason, List<SyncRunResponse> runs) {}
