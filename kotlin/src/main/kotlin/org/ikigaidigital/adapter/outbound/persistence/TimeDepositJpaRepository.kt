package org.ikigaidigital.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface TimeDepositJpaRepository : JpaRepository<TimeDepositEntity, Int> {

    @Query(
        value = """
            SELECT * FROM "timeDeposits"
            WHERE "lastInterestMonth" IS NULL OR "lastInterestMonth" < :month
            ORDER BY "id"
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true,
    )
    fun lockNextBatchToUpdate(month: LocalDate, batchSize: Int): List<TimeDepositEntity>
}
