package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.resources.ProductResource;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.mappers.ProductMapper;
import com.codewithmosh.store.repositories.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResource getProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return null;
        return productMapper.toResource(product);
    }
}
