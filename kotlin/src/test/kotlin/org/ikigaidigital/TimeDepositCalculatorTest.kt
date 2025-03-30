package org.ikigaidigital

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode

class TimeDepositCalculatorTest {

    @Test
    fun `updateBalance should calculate interest for basic plan`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(1, "basic", 1000.00, 45)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(BigDecimal(1000.83).setScale(2, RoundingMode.HALF_UP).toDouble())
    }

    @Test
    fun `updateBalance should calculate interest for student plan`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(2, "student", 1000.00, 365)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(BigDecimal(1002.50).setScale(2, RoundingMode.HALF_UP).toDouble())
    }

    @Test
    fun `updateBalance should calculate interest for premium plan`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(3, "premium", 1000.00, 46)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(BigDecimal(1004.17).setScale(2, RoundingMode.HALF_UP).toDouble())
    }

    @Test
    fun `updateBalance should not calculate interest for less than 30 days`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(4, "basic", 1000.00, 29)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(1000.00)
    }

    @Test
    fun `updateBalance should not calculate interest for student plan less than 366 days`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(5, "student", 1000.00, 365)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(BigDecimal(1002.50).setScale(2).toDouble())
    }

    @Test
    fun `updateBalance should not calculate interest for premium plan less than 45 days`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(6, "premium", 1000.00, 44)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(1000.00)
    }

    @Test
    fun `updateBalance should calculate interest for multiple deposits`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(7, "basic", 1000.00, 45),
            TimeDeposit(8, "student", 1000.00, 365),
            TimeDeposit(9, "premium", 1000.00, 46)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(BigDecimal(1000.83).setScale(2, RoundingMode.HALF_UP).toDouble())
        assertThat(plans[1].balance).isEqualTo(BigDecimal(1002.50).setScale(2, RoundingMode.HALF_UP).toDouble())
        assertThat(plans[2].balance).isEqualTo(BigDecimal(1004.17).setScale(2, RoundingMode.HALF_UP).toDouble())
    }

    @Test
    fun `updateBalance should handle zero balance`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(10, "basic", 0.00, 45)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(0.00)
    }

    @Test
    fun `updateBalance should handle negative balance`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(11, "basic", -1000.00, 45)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(BigDecimal(-1000.83).setScale(2, RoundingMode.HALF_UP).toDouble())
    }

    @Test
    fun `updateBalance should handle zero days`() {
        val calc = TimeDepositCalculator()
        val plans = listOf(
            TimeDeposit(12, "basic", 1000.00, 0)
        )
        calc.updateBalance(plans)
        assertThat(plans[0].balance).isEqualTo(1000.00)
    }
}