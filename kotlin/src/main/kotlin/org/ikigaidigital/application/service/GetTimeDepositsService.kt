package org.ikigaidigital.application.service

import org.ikigaidigital.TimeDepositPage
import org.ikigaidigital.application.port.inbound.GetTimeDepositsQuery
import org.ikigaidigital.application.port.outbound.TimeDepositPageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetTimeDepositsService(
    private val repository: TimeDepositPageRepository,
) : GetTimeDepositsQuery {

    @Transactional(readOnly = true)
    override fun getTimeDeposits(page: Int, size: Int): TimeDepositPage =
        repository.findPage(page.coerceAtLeast(0), size.coerceIn(1, MAX_PAGE_SIZE))

    companion object {
        const val MAX_PAGE_SIZE = 1000
    }
}
