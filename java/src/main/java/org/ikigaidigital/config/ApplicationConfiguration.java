package org.ikigaidigital.config;

import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.application.port.out.WithdrawalRepository;
import org.ikigaidigital.application.service.GetAllTimeDepositsService;
import org.ikigaidigital.application.service.UpdateAllBalancesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the application core. Domain and application classes stay free of stereotype annotations
 * (hexagonal architecture: the core does not depend on the framework's component model); only
 * {@code @Transactional} is used there, as a declarative transaction boundary.
 */
@Configuration(proxyBeanMethods = false)
class ApplicationConfiguration {

    @Bean
    TimeDepositCalculator timeDepositCalculator() {
        return new TimeDepositCalculator();
    }

    @Bean
    UpdateAllBalancesUseCase updateAllBalancesUseCase(TimeDepositRepository timeDepositRepository,
                                                      TimeDepositCalculator calculator) {
        return new UpdateAllBalancesService(timeDepositRepository, calculator);
    }

    @Bean
    GetAllTimeDepositsUseCase getAllTimeDepositsUseCase(TimeDepositRepository timeDepositRepository,
                                                        WithdrawalRepository withdrawalRepository) {
        return new GetAllTimeDepositsService(timeDepositRepository, withdrawalRepository);
    }
}
