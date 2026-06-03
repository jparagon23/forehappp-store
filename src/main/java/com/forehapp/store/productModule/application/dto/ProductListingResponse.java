package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.general.dto.PagedResponse;
import org.springframework.data.domain.Page;

public class ProductListingResponse extends PagedResponse<PublicProductSummaryResponse> {

    private final ProductFacetsResponse facets;

    public ProductListingResponse(Page<PublicProductSummaryResponse> page, ProductFacetsResponse facets) {
        super(page);
        this.facets = facets;
    }

    public ProductFacetsResponse getFacets() { return facets; }
}
