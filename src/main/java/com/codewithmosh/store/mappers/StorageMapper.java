package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.StorageDto;
import com.codewithmosh.store.entities.Storage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StorageMapper {
    @Mapping(target = "productId", source = "product.id")
    StorageDto toDto(Storage storage);

    Storage toEntity(StorageDto storageDto);

    @Mapping(target = "id", ignore = true)
    void update(StorageDto storageDto, @MappingTarget Storage storage);
}
