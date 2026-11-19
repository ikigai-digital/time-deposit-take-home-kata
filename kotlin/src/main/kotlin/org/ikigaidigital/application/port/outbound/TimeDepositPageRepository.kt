package org.ikigaidigital.application.port.outbound

import org.ikigaidigital.TimeDepositPage

fun interface TimeDepositPageRepository {
    fun findPage(page: Int, size: Int): TimeDepositPage
}
