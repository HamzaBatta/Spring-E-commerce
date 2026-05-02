package com.codewithmosh.store.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class StorageDto {
    private Long id;
    private String name;
    private String location;
    private List<StorageItemDto> items = new ArrayList<>();
}
