package com.codewithmosh.store.dtos.resources;

import lombok.Data;

@Data
public class StorageItemResource {
    private Long id;
    private Long productId;
    private Integer quantity;
}
