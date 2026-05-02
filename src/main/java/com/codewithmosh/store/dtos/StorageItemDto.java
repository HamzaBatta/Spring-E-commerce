package com.codewithmosh.store.dtos;

import lombok.Data;

@Data
public class StorageItemDto {
    private Long id;
    private Long productId;
    private Integer quantity;
}

