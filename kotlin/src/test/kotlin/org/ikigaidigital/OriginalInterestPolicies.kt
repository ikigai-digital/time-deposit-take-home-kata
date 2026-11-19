package org.ikigaidigital

import org.ikigaidigital.interest.MonthlyRateInterestPolicy

val ORIGINAL_INTEREST_POLICIES = listOf(
    MonthlyRateInterestPolicy("basic", annualRate = 0.01, interestStartsAfterDays = 30, interestEndsAfterDays = null),
    MonthlyRateInterestPolicy("student", annualRate = 0.03, interestStartsAfterDays = 30, interestEndsAfterDays = 365),
    MonthlyRateInterestPolicy("premium", annualRate = 0.05, interestStartsAfterDays = 45, interestEndsAfterDays = null),
)
