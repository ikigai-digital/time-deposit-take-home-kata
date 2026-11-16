package org.ikigaidigital.config;

import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.domain.interest.BasicInterestPolicy;
import org.ikigaidigital.domain.interest.InterestPolicy;
import org.ikigaidigital.domain.interest.PremiumInterestPolicy;
import org.ikigaidigital.domain.interest.StudentInterestPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InterestCalculationConfiguration {

    @Bean
    public InterestPolicy basicInterestPolicy() {
        return new BasicInterestPolicy();
    }

    @Bean
    public InterestPolicy studentInterestPolicy() {
        return new StudentInterestPolicy();
    }

    @Bean
    public InterestPolicy premiumInterestPolicy() {
        return new PremiumInterestPolicy();
    }

    @Bean
    public TimeDepositCalculator timeDepositCalculator(List<InterestPolicy> interestPolicies) {
        return new TimeDepositCalculator(interestPolicies);
    }
}
