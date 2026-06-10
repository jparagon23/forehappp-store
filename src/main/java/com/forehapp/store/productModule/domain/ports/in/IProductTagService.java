package com.forehapp.store.productModule.domain.ports.in;

import java.util.List;

public interface IProductTagService {
    List<String> getTags(Long productId, Long storeId, Long userId);
    List<String> setTags(Long productId, List<String> tags, Long storeId, Long userId);
}
