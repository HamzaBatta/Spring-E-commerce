package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.StorageItemDto;
import com.codewithmosh.store.entities.StorageItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StorageItemMapper {
    @Mapping(target = "productId", source = "product.id")
    StorageItemDto toDto(StorageItem item);
}

