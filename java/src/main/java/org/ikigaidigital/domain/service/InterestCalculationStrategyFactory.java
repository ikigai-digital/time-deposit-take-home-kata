package org.ikigaidigital.domain.service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class InterestCalculationStrategyFactory {
    private final Map<String, InterestCalculationStrategy> strategiesMap;
    private final InterestCalculationStrategy fallBackStrategy = new FallBackPlanStrategy();

    public InterestCalculationStrategyFactory() {
        this.strategiesMap = Stream.of(
            new BasicPlanStrategy(),
            new StudentPlanStrategy(),
            new PremiumPlanStrategy()
        ).collect(Collectors.toMap(InterestCalculationStrategy::getPlanType, Function.identity()));
    }

    public InterestCalculationStrategy getStrategy(String planType) {
        return strategiesMap.getOrDefault(planType, fallBackStrategy);
    }
}
