package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductImageService {
    ProductImageResponse upload(Long productId, MultipartFile file);
    void delete(Long productId, Long imageId);
    List<ProductImageResponse> getByProduct(Long productId);
}
