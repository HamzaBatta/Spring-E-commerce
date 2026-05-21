package com.codewithmosh.store.controllers;

import com.codewithmosh.store.services.ProductService;
import com.codewithmosh.store.services.ProductViewTracker;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/stats")
public class ProductStatsController {

    private final ProductViewTracker productViewTracker;
    private final ProductService productService;
    private final CacheManager cacheManager;

    public ProductStatsController(ProductViewTracker productViewTracker,
                                  ProductService productService,
                                  CacheManager cacheManager) {
        this.productViewTracker = productViewTracker;
        this.productService = productService;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/top")
    public ResponseEntity<?> getTopProducts() {
        List<Long> topIds = productViewTracker.getTopProductIds();
        List<Map<String, Object>> result = topIds.stream().map(id -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("productId", id);
            entry.put("views", productViewTracker.getViewCount(id));
            return entry;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/views")
    public ResponseEntity<?> getProductViews(@PathVariable Long id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", id);
        result.put("views", productViewTracker.getViewCount(id));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/benchmark")
    public ResponseEntity<?> benchmarkProductCache(@PathVariable Long id) {
        Cache cache = cacheManager.getCache("products");
        if (cache != null) cache.evict(id);

        long firstStart = System.nanoTime();
        var first = productService.getProductById(id);
        long firstMs = (System.nanoTime() - firstStart) / 1_000_000;

        long secondStart = System.nanoTime();
        var second = productService.getProductById(id);
        long secondMs = (System.nanoTime() - secondStart) / 1_000_000;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", id);
        result.put("firstCallMs", firstMs);
        result.put("secondCallMs", secondMs);
        result.put("firstFound", first != null);
        result.put("secondFound", second != null);
        return ResponseEntity.ok(result);
    }
}
