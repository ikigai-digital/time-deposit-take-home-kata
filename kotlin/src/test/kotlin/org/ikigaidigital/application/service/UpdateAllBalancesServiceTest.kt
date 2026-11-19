package org.ikigaidigital.application.service

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.ORIGINAL_INTEREST_POLICIES
import org.ikigaidigital.TimeDepositRecord
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

class UpdateAllBalancesServiceTest {

    private class InMemoryTimeDepositRepository(deposits: List<TimeDepositRecord>) : TimeDepositRepository {
        val deposits = deposits.associateBy { it.id }.toMutableMap()
        val batchSizes = mutableListOf<Int>()

        override fun findBatchToUpdate(month: YearMonth, batchSize: Int): List<TimeDepositRecord> =
            deposits.values.filter { deposit -> deposit.lastInterestMonth?.let { it < month } ?: true }
                .take(batchSize)
                .also { batchSizes += it.size }

        override fun save(deposit: TimeDepositRecord) {
            deposits[deposit.id] = deposit
        }

        fun balances() = deposits.values.map { it.balance.toPlainString() }
    }

    private fun deposit(id: Int, planType: String, days: Int) = TimeDepositRecord(id, planType, days, BigDecimal("1000.00"), null)

    private val threeDeposits = listOf(deposit(1, "basic", 31), deposit(2, "premium", 10), deposit(3, "student", 31))

    private fun service(repository: TimeDepositRepository, now: String) =
        UpdateAllBalancesService(
            deposits = repository,
            plans = { ORIGINAL_INTEREST_POLICIES },
            transaction = TransactionOperations.withoutTransaction(),
            clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC),
            batchSize = 2,
        )

    @Test
    fun `adds monthly interest to every deposit batch by batch`() {
        val repository = InMemoryTimeDepositRepository(threeDeposits)

        service(repository, "2026-11-20T10:00:00Z").updateAllBalances()

        assertThat(repository.batchSizes).containsExactly(2, 1)
        assertThat(repository.balances()).containsExactly("1000.83", "1000.00", "1002.50")
    }

    @Test
    fun `adds interest only once per month`() {
        val repository = InMemoryTimeDepositRepository(threeDeposits)

        repeat(2) { service(repository, "2026-11-20T10:00:00Z").updateAllBalances() }
        service(repository, "2026-12-01T00:00:00Z").updateAllBalances()

        assertThat(repository.balances()).containsExactly("1001.66", "1000.00", "1005.01")
    }

    @Test
    fun `stops at an empty batch after an exactly full one`() {
        val repository = InMemoryTimeDepositRepository(threeDeposits.take(2))

        service(repository, "2026-11-20T10:00:00Z").updateAllBalances()

        assertThat(repository.batchSizes).containsExactly(2, 0)
    }
}
