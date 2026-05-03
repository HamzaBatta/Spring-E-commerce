package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.DailySalesReportEntity;
import com.codewithmosh.store.repositories.DailySalesReportRepository;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class DailySalesReportStore {
    private final AtomicReference<BatchReport> last = new AtomicReference<>();
    private final DailySalesReportRepository repository;

    public DailySalesReportStore(DailySalesReportRepository repository) {
        this.repository = repository;
    }

    public void setLast(BatchReport r) { last.set(r); }
    public BatchReport getLast() { return last.get(); }

    public DailySalesReportEntity getLastFromDb() {
        return repository.findTopByOrderByCreatedAtDesc().orElse(null);
    }
}
