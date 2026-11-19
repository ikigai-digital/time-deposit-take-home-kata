package org.ikigaidigital.application.port.inbound

import org.ikigaidigital.TimeDepositPage

fun interface GetTimeDepositsQuery {
    fun getTimeDeposits(page: Int, size: Int): TimeDepositPage
}
