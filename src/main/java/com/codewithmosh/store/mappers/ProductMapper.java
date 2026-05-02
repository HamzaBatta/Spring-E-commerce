package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.requests.CreateProductRequest;
import com.codewithmosh.store.dtos.requests.UpdateProductRequest;
import com.codewithmosh.store.dtos.resources.ProductResource;
import com.codewithmosh.store.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "categoryId", source = "category.id")
    ProductResource toResource(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    void update(UpdateProductRequest request, @MappingTarget Product product);
}
