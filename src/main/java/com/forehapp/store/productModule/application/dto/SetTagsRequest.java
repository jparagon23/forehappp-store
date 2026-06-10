package com.forehapp.store.productModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SetTagsRequest {

    @NotNull(message = "Tags list is required")
    @Size(max = 20, message = "A product can have at most 20 tags")
    private List<@NotBlank(message = "Tag cannot be blank") @Size(max = 50, message = "Tag cannot exceed 50 characters") String> tags;
}
