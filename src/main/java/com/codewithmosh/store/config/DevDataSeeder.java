package com.codewithmosh.store.config;

import com.codewithmosh.store.entities.Category;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.entities.Storage;
import com.codewithmosh.store.entities.StorageItem;
import com.codewithmosh.store.repositories.CategoryRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.repositories.StorageItemRepository;
import com.codewithmosh.store.repositories.StorageRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@AllArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StorageRepository storageRepository;
    private final StorageItemRepository storageItemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedCategoriesAndProducts();
        seedStoragesAndItems();
    }

    private void seedCategoriesAndProducts() {
        if (productRepository.count() > 0) return; // already seeded

        var electronics = categoryRepository.save(new Category("Electronics"));
        var books = categoryRepository.save(new Category("Books"));
        var clothing = categoryRepository.save(new Category("Clothing"));

        List<Product> products = List.of(
                Product.builder().name("Wireless Headphones").description("Noise cancelling").price(new BigDecimal("99.99")).category(electronics).build(),
                Product.builder().name("USB-C Charger").description("Fast charging 30W").price(new BigDecimal("19.99")).category(electronics).build(),
                Product.builder().name("Java Programming").description("Comprehensive guide").price(new BigDecimal("39.99")).category(books).build(),
                Product.builder().name("T-Shirt").description("100% cotton").price(new BigDecimal("14.99")).category(clothing).build()
        );

        productRepository.saveAll(products);
        System.out.println("DevDataSeeder: Seeded categories and products: " + products.size());
    }

    private void seedStoragesAndItems() {
        if (storageRepository.count() > 0) return; // already seeded

        var w1 = storageRepository.save(Storage.builder().name("Warehouse A").location("New York").build());
        var w2 = storageRepository.save(Storage.builder().name("Warehouse B").location("Los Angeles").build());

        var allProducts = productRepository.findAll();
        if (allProducts.isEmpty()) return;

        int idx = 0;
        for (Product p : allProducts) {
            var item1 = new StorageItem();
            item1.setStorage(w1);
            item1.setProduct(p);
            item1.setQuantity(Math.max(10, 50 - idx * 5));

            var item2 = new StorageItem();
            item2.setStorage(w2);
            item2.setProduct(p);
            item2.setQuantity(30 + idx * 3);

            storageItemRepository.save(item1);
            storageItemRepository.save(item2);
            idx++;
        }

        System.out.println("DevDataSeeder: Seeded storages and storage items");
    }
}
