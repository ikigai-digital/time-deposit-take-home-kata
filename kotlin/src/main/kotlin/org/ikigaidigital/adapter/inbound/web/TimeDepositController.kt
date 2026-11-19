package org.ikigaidigital.adapter.inbound.web

import org.ikigaidigital.adapter.inbound.web.api.TimeDepositsApi
import org.ikigaidigital.adapter.inbound.web.model.TimeDepositPage
import org.ikigaidigital.application.port.inbound.GetTimeDepositsQuery
import org.ikigaidigital.application.port.inbound.UpdateAllBalancesUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class TimeDepositController(
    private val getTimeDeposits: GetTimeDepositsQuery,
    private val updateAllBalances: UpdateAllBalancesUseCase,
) : TimeDepositsApi {

    override fun getTimeDeposits(page: Int, size: Int): ResponseEntity<TimeDepositPage> =
        ResponseEntity.ok(TimeDepositWebMapper.toDto(getTimeDeposits.getTimeDeposits(page, size)))

    override fun updateBalances(): ResponseEntity<Unit> {
        updateAllBalances.updateAllBalances()
        return ResponseEntity.noContent().build()
    }
}
