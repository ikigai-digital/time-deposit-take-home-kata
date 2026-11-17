package org.ikigaidigital.timedeposit.infrastructure;

import org.ikigaidigital.timedeposit.infrastructure.entity.TimeDepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface TimeDepositJpaRepository extends JpaRepository<TimeDepositEntity, Integer> {
}
