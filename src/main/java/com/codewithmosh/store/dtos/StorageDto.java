package com.codewithmosh.store.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class StorageDto {
    private Long id;
    private String name;
    private String location;
    private Long productId;
    private Integer quantity;
}
