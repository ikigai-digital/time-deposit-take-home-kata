package org.ikigaidigital.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTimeDepositRepository extends JpaRepository<TimeDepositEntity, Integer> {
}
