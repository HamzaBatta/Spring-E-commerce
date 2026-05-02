package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.resources.StorageResource;
import com.codewithmosh.store.entities.Storage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = StorageItemMapper.class)
public interface StorageMapper {
    StorageResource toResource(Storage storage);
}
