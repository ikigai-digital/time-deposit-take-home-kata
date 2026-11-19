package org.ikigaidigital

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.assertj.core.api.Assertions.within
import org.ikigaidigital.interest.InterestPolicy
import org.ikigaidigital.interest.MonthlyRateInterestPolicy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TimeDepositCalculatorTest {

    private val calculator = TimeDepositCalculator(ORIGINAL_INTEREST_POLICIES)

    @ParameterizedTest(name = "{0} plan, {1} days, balance {2} -> {3}")
    @CsvSource(
        "basic,    30,  1000.00, 1000.00",
        "student,  30,  1000.00, 1000.00",
        "premium,  30,  1000.00, 1000.00",
        "basic,    31,  1000.00, 1000.83",
        "basic,    45,  1234567.00, 1235595.81",
        "basic,    3650, 1000.00, 1000.83",
        "student,  31,  1000.00, 1002.50",
        "student,  365, 1000.00, 1002.50",
        "student,  366, 1000.00, 1000.00",
        "premium,  45,  1000.00, 1000.00",
        "premium,  46,  1000.00, 1004.17",
        "premium,  400, 1000.00, 1004.17",
        "gold,     100, 1000.00, 1000.00",
        "BASIC,    100, 1000.00, 1000.00",
    )
    fun `updates balance according to plan type`(planType: String, days: Int, balance: Double, expected: Double) {
        val deposit = TimeDeposit(1, planType, balance, days)

        calculator.updateBalance(listOf(deposit))

        assertThat(deposit.balance).isCloseTo(expected, within(1e-9))
    }

    @Test
    fun `rounds interest half up to two decimals`() {
        val deposits = listOf(
            TimeDeposit(1, "basic", 150.00, 31),
            TimeDeposit(2, "basic", 1818.00, 31),
        )

        calculator.updateBalance(deposits)

        assertThat(deposits[0].balance).isCloseTo(150.13, within(1e-9))
        assertThat(deposits[1].balance).isCloseTo(1819.51, within(1e-9))
    }

    @Test
    fun `monthly interest is the amount updateBalance adds, in cents`() {
        val deposit = TimeDeposit(1, "basic", 1818.00, 31)

        assertThat(calculator.monthlyInterest(deposit)).isEqualTo("1.51")
        assertThat(calculator.monthlyInterest(TimeDeposit(2, "basic", 1000.0, 30))).isEqualTo("0.00")
        assertThat(deposit.balance).isEqualTo(1818.00)
    }

    @Test
    fun `updates every deposit in the list in place`() {
        val deposits = listOf(
            TimeDeposit(1, "basic", 1000.0, 31),
            TimeDeposit(2, "student", 1000.0, 31),
            TimeDeposit(3, "premium", 1000.0, 46),
            TimeDeposit(4, "premium", 1000.0, 10),
        )

        calculator.updateBalance(deposits)

        assertThat(deposits.map { it.balance })
            .usingElementComparator { a, b -> if (Math.abs(a - b) < 1e-9) 0 else a.compareTo(b) }
            .containsExactly(1000.83, 1002.50, 1004.17, 1000.0)
    }

    @Test
    fun `supports custom interest policies`() {
        val flatFee = object : InterestPolicy {
            override val planType = "gold"
            override fun monthlyInterest(deposit: TimeDeposit) = 10.0
        }
        val deposit = TimeDeposit(1, "gold", 1000.0, 1)

        TimeDepositCalculator(listOf(flatFee)).updateBalance(listOf(deposit))

        assertThat(deposit.balance).isEqualTo(1010.0)
    }

    @Test
    fun `rejects two policies for the same plan type`() {
        assertThatIllegalArgumentException().isThrownBy {
            TimeDepositCalculator(ORIGINAL_INTEREST_POLICIES + MonthlyRateInterestPolicy("basic", 0.02, 30, null))
        }
    }

    @Test
    fun `handles an empty list`() {
        calculator.updateBalance(emptyList())
    }
}
