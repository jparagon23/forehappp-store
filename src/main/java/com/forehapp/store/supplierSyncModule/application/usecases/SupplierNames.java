package com.forehapp.store.supplierSyncModule.application.usecases;

import java.text.Normalizer;
import java.util.Locale;

public final class SupplierNames {

    private SupplierNames() {}

    /** Identity key of a supplier product: lowercase, no accents, punctuation collapsed to single spaces. */
    public static String normalize(String name) {
        if (name == null) return "";
        String noAccents = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccents.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }
}
