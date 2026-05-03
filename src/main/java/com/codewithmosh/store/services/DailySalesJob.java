package com.codewithmosh.store.services;

import com.codewithmosh.store.strategy.StrategySelector;
import com.codewithmosh.store.strategy.StrategyContext;
import com.codewithmosh.store.entities.DailySalesReportEntity;
import com.codewithmosh.store.repositories.DailySalesReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailySalesJob {

    private final StrategySelector strategySelector;
    private final StrategyContext strategyContext;
    private final DailySalesReportStore reportStore;
    private final DailySalesReportRepository reportRepository;

    @Value("${app.batch.enabled:true}")
    private boolean enabled;

    public DailySalesJob(StrategySelector strategySelector, StrategyContext strategyContext,
                         DailySalesReportStore reportStore, DailySalesReportRepository reportRepository) {
        this.strategySelector = strategySelector;
        this.strategyContext = strategyContext;
        this.reportStore = reportStore;
        this.reportRepository = reportRepository;
    }

    // scheduled daily at 03:00 server time by default
    @Scheduled(cron = "0 0 3 * * ?")
    public void runScheduled() {
        if (!enabled) return;
        // run with default strategy (StrategyContext empty)
        var processor = strategySelector.resolve(DailySalesProcessor.class);
        var report = processor.process();
        reportStore.setLast(report);
        // persist
        persist(report);
    }

    public BatchReport runManual(String strategyHeader) {
        try {
            if (strategyHeader != null && !strategyHeader.isBlank()) {
                strategyContext.set(strategyHeader);
            }
            var processor = strategySelector.resolve(DailySalesProcessor.class);
            var report = processor.process();
            reportStore.setLast(report);
            // persist
            persist(report);
            return report;
        } finally {
            strategyContext.clear();
        }
    }

    private void persist(BatchReport r) {
        try {
            var entity = new DailySalesReportEntity(r.getProcessedCount(), r.getTotalRevenue(), r.getDurationMs(), r.getStrategy());
            reportRepository.save(entity);
        } catch (Exception e) {
            // never let persistence failure stop the job; log and continue
            System.err.println("Failed to persist daily sales report: " + e.getMessage());
        }
    }
}
