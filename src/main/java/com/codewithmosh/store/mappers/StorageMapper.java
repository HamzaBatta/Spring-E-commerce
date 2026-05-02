package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.StorageDto;
import com.codewithmosh.store.entities.Storage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = StorageItemMapper.class)
public interface StorageMapper {
    StorageDto toDto(Storage storage);

    Storage toEntity(StorageDto storageDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    void update(StorageDto storageDto, @MappingTarget Storage storage);
}
