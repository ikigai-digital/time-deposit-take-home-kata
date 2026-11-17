package org.ikigaidigital.timedeposit.infrastructure;

import org.ikigaidigital.timedeposit.infrastructure.entity.WithdrawalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface WithdrawalJpaRepository extends JpaRepository<WithdrawalEntity, Integer> {
}
