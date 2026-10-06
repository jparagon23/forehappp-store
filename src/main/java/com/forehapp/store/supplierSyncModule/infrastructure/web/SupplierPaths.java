package com.forehapp.store.supplierSyncModule.infrastructure.web;

import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;

final class SupplierPaths {

    private SupplierPaths() {}

    /** Path segment to supplier, case-insensitive ("profitness" or "PROFITNESS"). */
    static SupplierCode parse(String value) {
        try {
            return SupplierCode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(ErrorCode.SUPPLIER_UNKNOWN, "Unknown supplier: " + value);
        }
    }
}
