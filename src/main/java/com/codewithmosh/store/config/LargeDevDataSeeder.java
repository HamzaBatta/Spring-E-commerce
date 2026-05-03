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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@AllArgsConstructor
@Order(2)
public class LargeDevDataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StorageRepository storageRepository;
    private final StorageItemRepository storageItemRepository;

    private final Random rnd = new Random(42);
    private final Faker faker = new Faker(rnd);

    @Override
    @Transactional
    public void run(String... args) {
        long existing = productRepository.count();
        if (existing >= 100) {
            System.out.println("LargeDevDataSeeder: already have " + existing + " products — skipping");
            return;
        }

        // ensure categories exist
        List<Category> categories = ensureCategories();

        // generate products up to 100 total
        int toCreate = (int) (100 - existing);
        List<Product> batch = new ArrayList<>(toCreate);

        for (int i = 1; i <= toCreate; i++) {
            Category cat = categories.get(rnd.nextInt(categories.size()));
            String name = faker.commerce().productName() + " " + (i + (int) existing);
            String desc = faker.lorem().sentence(6);
            double priceD = 5 + rnd.nextDouble() * 495;
            BigDecimal price = BigDecimal.valueOf(priceD).setScale(2, RoundingMode.HALF_UP);

            Product p = Product.builder()
                    .name(name)
                    .description(desc)
                    .price(price)
                    .category(cat)
                    .build();
            batch.add(p);
        }

        productRepository.saveAll(batch);
        System.out.println("LargeDevDataSeeder: created " + batch.size() + " products");

        // create 3 storages if not exists
        List<Storage> storages = new ArrayList<>();
        if (storageRepository.count() < 3) {
            storages.add(storageRepository.save(Storage.builder().name("Warehouse A").location("New York").build()));
            storages.add(storageRepository.save(Storage.builder().name("Warehouse B").location("Los Angeles").build()));
            storages.add(storageRepository.save(Storage.builder().name("Warehouse C").location("Chicago").build()));
        } else {
            storages = storageRepository.findAll();
            if (storages.size() > 3) storages = storages.subList(0, 3);
        }

        // assign quantities for all products across storages
        List<Product> allProducts = productRepository.findAll();
        for (Product p : allProducts) {
            for (Storage s : storages) {
                StorageItem item = new StorageItem();
                item.setStorage(s);
                item.setProduct(p);
                // distribute quantities, 10..200
                item.setQuantity(10 + rnd.nextInt(191));
                storageItemRepository.save(item);
            }
        }

        System.out.println("LargeDevDataSeeder: assigned storage items for " + allProducts.size() + " products across " + storages.size() + " storages");
    }

    private List<Category> ensureCategories() {
        List<Category> existing = new ArrayList<>();
        categoryRepository.findAll().forEach(existing::add);
        for (String name : List.of("Electronics", "Books", "Clothing", "Home", "Sports", "Toys")) {
            Category found = existing.stream().filter(c -> name.equalsIgnoreCase(c.getName())).findFirst().orElse(null);
            if (found == null) {
                found = categoryRepository.save(new Category(name));
                existing.add(found);
            }
        }
        return existing;
    }
}
