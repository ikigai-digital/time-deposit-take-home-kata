package org.ikigaidigital.timedeposit.domain.interest.calculator;

import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;
import org.ikigaidigital.timedeposit.domain.interest.calculator.policy.BasicInterestPolicy;
import org.ikigaidigital.timedeposit.domain.interest.calculator.policy.InterestPolicy;
import org.ikigaidigital.timedeposit.domain.interest.calculator.policy.PremiumInterestPolicy;
import org.ikigaidigital.timedeposit.domain.interest.calculator.policy.StudentInterestPolicy;
import org.springframework.stereotype.Service;

@Service
public class InterestCalculatorImpl implements InterestCalculator {

  private static final int INTEREST_FREE_DAYS = 30;
  private static final int MONEY_SCALE = 2;
  private static final double NO_INTEREST = 0;
  private static final String PLAN_TYPE_REQUIRED = "planType must not be null";

  private final Map<String, InterestPolicy> policiesByPlanType;

  public InterestCalculatorImpl() {
    this.policiesByPlanType = Stream.of(new BasicInterestPolicy(), new StudentInterestPolicy(),
            new PremiumInterestPolicy())
        .collect(toMap(InterestPolicy::planType, Function.identity()));
  }

  public BigDecimal calculateMonthlyInterest(TimeDeposit timeDeposit) {
    return new BigDecimal(unroundedMonthlyInterest(timeDeposit)).setScale(MONEY_SCALE,
        RoundingMode.HALF_UP);
  }

  private double unroundedMonthlyInterest(TimeDeposit timeDeposit) {
    if (timeDeposit.getDays() <= INTEREST_FREE_DAYS) {
      return NO_INTEREST;
    }
    var interestPolicy = policiesByPlanType.get(
        Objects.requireNonNull(timeDeposit.getPlanType(), PLAN_TYPE_REQUIRED));

    return interestPolicy == null ? NO_INTEREST : interestPolicy.monthlyInterest(timeDeposit);
  }
}
