package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.requests.CreateProductRequest;
import com.codewithmosh.store.dtos.requests.UpdateProductRequest;
import com.codewithmosh.store.dtos.resources.ProductResource;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.mappers.ProductMapper;
import com.codewithmosh.store.repositories.CategoryRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.services.ProductService;
import com.codewithmosh.store.services.ProductViewTracker;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/products")
@RestController
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final ProductViewTracker productViewTracker;

    @GetMapping
    public org.springframework.data.domain.Page<ProductResource> getAllProducts(
            @RequestParam(required = false, name = "categoryId") Byte categoryId,
            @RequestParam(required = false, defaultValue = "0", name = "page") int page,
            @RequestParam(required = false, defaultValue = "10", name = "size") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Product> products = (categoryId != null)
                ? productRepository.findByCategoryId(categoryId, pageable)
                : productRepository.findAllWithCategory(pageable);
        return products.map(productMapper::toResource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResource> getProduct(@PathVariable Long id) {
        productViewTracker.trackView(id);
        var product = productService.getProductById(id);
        if (product == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<ProductResource> createProduct(
            UriComponentsBuilder uriBuilder,
            @Valid @RequestBody CreateProductRequest request) {
        var category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        if (category == null) return ResponseEntity.badRequest().build();

        var product = productMapper.toEntity(request);
        product.setCategory(category);
        productRepository.save(product);

        var resource = productMapper.toResource(product);
        var uri = uriBuilder.path("/products/{id}").buildAndExpand(resource.getId()).toUri();
        return ResponseEntity.created(uri).body(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResource> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        var category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        if (category == null) return ResponseEntity.badRequest().build();

        var product = productRepository.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();

        productMapper.update(request, product);
        product.setCategory(category);
        productRepository.save(product);
        return ResponseEntity.ok(productMapper.toResource(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        var product = productRepository.findById(id).orElse(null);
        if (product == null) return ResponseEntity.notFound().build();
        productRepository.delete(product);
        return ResponseEntity.noContent().build();
    }
}
