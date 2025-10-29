package org.ikigaidigital.repository;

import org.ikigaidigital.entity.TimeDepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeDepositRepository extends JpaRepository<TimeDepositEntity, Integer> {
}
