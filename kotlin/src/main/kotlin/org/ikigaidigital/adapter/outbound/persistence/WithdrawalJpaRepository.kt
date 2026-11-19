package org.ikigaidigital.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface WithdrawalJpaRepository : JpaRepository<WithdrawalEntity, Int> {

    fun findByTimeDepositIdInOrderByDateAscIdAsc(timeDepositIds: Collection<Int>): List<WithdrawalEntity>
}
