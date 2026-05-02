package com.codewithmosh.store.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStorageRequest {
    @NotBlank(message = "Storage name is required")
    private String name;

    @NotBlank(message = "Storage location is required")
    private String location;
}

