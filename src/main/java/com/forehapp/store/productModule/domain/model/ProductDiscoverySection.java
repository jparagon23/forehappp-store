package com.forehapp.store.productModule.domain.model;

import java.util.List;

public record ProductDiscoverySection(Long categoryId, long totalInCategory, List<Product> products) {}
