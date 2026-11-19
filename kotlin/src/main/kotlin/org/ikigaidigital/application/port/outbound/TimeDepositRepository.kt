package org.ikigaidigital.application.port.outbound

import org.ikigaidigital.TimeDepositRecord
import java.time.YearMonth

interface TimeDepositRepository {
    fun findBatchToUpdate(month: YearMonth, batchSize: Int): List<TimeDepositRecord>

    fun save(deposit: TimeDepositRecord)
}
