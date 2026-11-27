package org.ikigaidigital;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Assumptions (README "Interest Calculation" is ambiguous here; all match the legacy calculator):
 * <ul>
 *   <li>Rates are annual, accrued monthly: "1% interest" means {@code balance * 0.01 / 12} per month.
 *       Stored as fractions (0.01, not 1) to keep the double arithmetic identical to the legacy code.</li>
 *   <li>"After N days" is exclusive: interest starts on day N + 1.</li>
 *   <li>"After 1 year" means after 365 days; leap years are ignored.</li>
 *   <li>Plan codes match exactly and case-sensitively; unknown codes earn no interest.</li>
 * </ul>
 */
enum PlanType {
    // No interest for the first 30 days (README: applies to all existing plans).
    BASIC("basic", 0.01, 30, PlanType.NO_LIMIT),
    // Interest stops after 1 year (365 days).
    STUDENT("student", 0.03, 30, 365),
    // Interest starts after 45 days, which already covers the general 30-day rule.
    PREMIUM("premium", 0.05, 45, PlanType.NO_LIMIT);

    private static final int NO_LIMIT = Integer.MAX_VALUE;

    private static final int EARLIEST_INTEREST_START = Arrays.stream(values())
            .mapToInt(plan -> plan.interestStartsAfterDays)
            .min()
            .orElse(NO_LIMIT);

    private final String code;
    private final double annualInterestRate;
    private final int interestStartsAfterDays;
    private final int interestEndsAfterDays;


    PlanType(String code, double annualInterestRate, int interestStartsAfterDays, int interestEndsAfterDays) {
        this.code = code;
        this.annualInterestRate = annualInterestRate;
        this.interestStartsAfterDays = interestStartsAfterDays;
        this.interestEndsAfterDays = interestEndsAfterDays;
    }

    /**
     * True while the deposit is too young for any plan to earn interest (the README's
     * "no interest for the first 30 days for any existing plans"), derived from the plans themselves.
     */
    static boolean noPlanEarnsInterestAt(int days) {
        return days <= EARLIEST_INTEREST_START;
    }

    /**
     * Resolves a plan from its code.
     *
     * @throws NullPointerException if {@code code} is null, as the legacy {@code planType.equals(..)} did
     */
    static Optional<PlanType> fromCode(String code) {
        Objects.requireNonNull(code, "planType");
        return Arrays.stream(values())
                .filter(plan -> plan.code.equals(code))
                .findFirst();
    }

    String code() {
        return code;
    }

    double annualInterestRate() {
        return annualInterestRate;
    }

    boolean earnsInterest(int days) {
        return days > interestStartsAfterDays && days <= interestEndsAfterDays;
    }
}
