package org.ikigaidigital.persistence.repository

import org.ikigaidigital.persistence.entity.WithdrawalEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WithdrawalRepository : JpaRepository<WithdrawalEntity, Int>