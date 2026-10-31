package org.ikigaidigital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Starts the application and keeps Spring wiring outside the plain Java calculation classes. */
@SpringBootApplication
public class TimeDepositApplication {
    public static void main(String[] args) {
        // The root package lets Spring discover the service and adapters in its subpackages.
        SpringApplication.run(TimeDepositApplication.class, args);
    }

    @Bean
    public TimeDepositCalculator timeDepositCalculator() {
        // Register the existing default calculator so Spring can inject it into the service.
        return new TimeDepositCalculator();
    }
}
