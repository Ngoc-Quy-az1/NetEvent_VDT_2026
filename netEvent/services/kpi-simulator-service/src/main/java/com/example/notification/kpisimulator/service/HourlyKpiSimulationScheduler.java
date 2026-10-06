package com.example.notification.kpisimulator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HourlyKpiSimulationScheduler {
    private final KpiSimulationService simulationService;

    @Value("${kpi.simulator.enabled:true}")
    private boolean enabled;

    @Scheduled(cron = "${kpi.simulator.cron:10 * * * * *}", zone = "${kpi.simulator.zone:Asia/Ho_Chi_Minh}")
    public void simulateHourly() {
        if (!enabled) return;
        try {
            simulationService.simulateCurrentHour();
        } catch (Exception exception) {
            log.error("Hourly KPI simulation failed", exception);
        }
    }
}
