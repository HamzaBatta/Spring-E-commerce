package com.codewithmosh.store.config;

import com.codewithmosh.store.entities.OrderStatus;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.entities.Storage;
import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.repositories.StorageRepository;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds large volumes of users and orders so the batch report processors
 * (SimpleDailySalesProcessor vs ChunkedParallelDailySalesProcessor) operate
 * on enough data to show a meaningful performance difference.
 *
 * Skips seeding if orders already exceed ORDERS_THRESHOLD.
 * Depends on LargeDevDataSeeder (@Order(2)) to have products and storages ready.
 */
@Component
@AllArgsConstructor
@Order(3)
public class BatchDataSeeder implements CommandLineRunner {

    private static final int TARGET_USERS      = 500;
    private static final int TARGET_ORDERS     = 100_000;
    private static final int ORDERS_THRESHOLD  = 50_000;
    private static final int BATCH_SIZE        = 1_000;

    private final UserRepository    userRepository;
    private final OrderRepository   orderRepository;
    private final ProductRepository productRepository;
    private final StorageRepository storageRepository;
    private final PasswordEncoder   passwordEncoder;

    private final Random rnd  = new Random(99);
    private final Faker  faker = new Faker(rnd);

    @Override
    @Transactional
    public void run(String... args) {
        long existingOrders = orderRepository.count();
        if (existingOrders >= ORDERS_THRESHOLD) {
            System.out.printf("BatchDataSeeder: already have %d orders (≥ %d threshold) — skipping%n",
                    existingOrders, ORDERS_THRESHOLD);
            return;
        }

        List<Product> products = productRepository.findAll();
        List<Storage> storages = storageRepository.findAll();

        if (products.isEmpty() || storages.isEmpty()) {
            System.out.println("BatchDataSeeder: no products or storages found — run LargeDevDataSeeder first");
            return;
        }

        // ── 1. Create users ──────────────────────────────────────────────────
        List<User> users = seedUsers(TARGET_USERS);
        System.out.printf("BatchDataSeeder: ensured %d users%n", users.size());

        // ── 2. Create orders in batches ──────────────────────────────────────
        int toCreate = (int) (TARGET_ORDERS - existingOrders);
        System.out.printf("BatchDataSeeder: creating %d orders (have %d, target %d)%n",
                toCreate, existingOrders, TARGET_ORDERS);

        int created = 0;
        List<com.codewithmosh.store.entities.Order> batch = new ArrayList<>(BATCH_SIZE);

        OrderStatus[] completedStatuses = {
            OrderStatus.DELIVERED, OrderStatus.CONFIRMED, OrderStatus.SHIPPED
        };

        for (int i = 0; i < toCreate; i++) {
            User    user    = users.get(rnd.nextInt(users.size()));
            Storage storage = storages.get(rnd.nextInt(storages.size()));
            OrderStatus status = completedStatuses[rnd.nextInt(completedStatuses.length)];

            com.codewithmosh.store.entities.Order order = new com.codewithmosh.store.entities.Order();
            order.setUser(user);
            order.setStorage(storage);
            order.setStatus(status);

            // 1–4 items per order
            int itemCount = 1 + rnd.nextInt(4);
            for (int j = 0; j < itemCount; j++) {
                Product product  = products.get(rnd.nextInt(products.size()));
                int     quantity = 1 + rnd.nextInt(5);
                BigDecimal unitPrice = product.getPrice()
                        .multiply(BigDecimal.valueOf(0.9 + rnd.nextDouble() * 0.2))
                        .setScale(2, RoundingMode.HALF_UP);
                order.addItem(product, quantity, unitPrice);
            }

            batch.add(order);

            if (batch.size() == BATCH_SIZE) {
                orderRepository.saveAll(batch);
                created += batch.size();
                batch.clear();
                System.out.printf("BatchDataSeeder: saved %d / %d orders...%n", created, TARGET_ORDERS);
            }
        }

        if (!batch.isEmpty()) {
            orderRepository.saveAll(batch);
            created += batch.size();
        }

        System.out.printf("BatchDataSeeder: done — created %d orders for %d users%n", created, users.size());
    }

    /**
     * Returns up to {@code target} users, creating new ones if needed.
     * Uses a fixed password so tests/demo logins work predictably.
     */
    private List<User> seedUsers(int target) {
        List<User> existing = userRepository.findAll();
        if (existing.size() >= target) return existing;

        int toCreate = target - existing.size();
        List<User> newUsers = new ArrayList<>(toCreate);
        String encodedPassword = passwordEncoder.encode("Password123!");

        for (int i = 0; i < toCreate; i++) {
            String email = faker.internet().emailAddress().replace("@", "_" + rnd.nextInt(999_999) + "@");
            if (userRepository.existsByEmail(email)) continue;

            User user = User.builder()
                    .name(faker.name().fullName())
                    .email(email)
                    .password(encodedPassword)
                    .build();
            newUsers.add(user);
        }

        userRepository.saveAll(newUsers);
        existing.addAll(newUsers);
        return existing;
    }
}
