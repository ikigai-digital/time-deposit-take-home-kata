package org.ikigaidigital.persistence.repository

import org.ikigaidigital.persistence.entity.TimeDepositEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TimeDepositRepository : JpaRepository<TimeDepositEntity, Int>