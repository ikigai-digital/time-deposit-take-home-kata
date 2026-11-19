package org.ikigaidigital.adapter.inbound.web

import org.ikigaidigital.TimeDepositDetails
import org.ikigaidigital.TimeDepositPage
import org.ikigaidigital.Withdrawal
import org.ikigaidigital.adapter.inbound.web.model.TimeDeposit as TimeDepositDto
import org.ikigaidigital.adapter.inbound.web.model.TimeDepositPage as TimeDepositPageDto
import org.ikigaidigital.adapter.inbound.web.model.Withdrawal as WithdrawalDto

internal object TimeDepositWebMapper {

    fun toDto(page: TimeDepositPage): TimeDepositPageDto =
        TimeDepositPageDto(
            items = page.items.map(::toDto),
            page = page.page,
            propertySize = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )

    private fun toDto(details: TimeDepositDetails): TimeDepositDto =
        TimeDepositDto(
            id = details.id,
            planType = details.planType,
            balance = details.balance,
            days = details.days,
            withdrawals = details.withdrawals.map(::toDto),
        )

    private fun toDto(withdrawal: Withdrawal): WithdrawalDto =
        WithdrawalDto(id = withdrawal.id, amount = withdrawal.amount, date = withdrawal.date)
}
