package com.codewithmosh.store.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStorageRequest {
    @NotBlank(message = "Storage name is required")
    private String name;

    @NotBlank(message = "Storage location is required")
    private String location;
}
