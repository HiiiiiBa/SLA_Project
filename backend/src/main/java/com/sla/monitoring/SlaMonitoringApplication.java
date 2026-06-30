package com.sla.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlaMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlaMonitoringApplication.class, args);
    }
}
