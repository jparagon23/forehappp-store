package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.CreateProductRequestDto;
import com.forehapp.store.productModule.application.dto.ProductResponse;

public interface IProductService {
    ProductResponse createProduct(CreateProductRequestDto dto, Long userId);
}
