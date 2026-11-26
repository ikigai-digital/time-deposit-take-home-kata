package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.adapter.out.persistence.entity.TimeDepositEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeDepositJpaRepository extends JpaRepository<TimeDepositEntity, Integer> {

    @EntityGraph(attributePaths = "withdrawals")
    List<TimeDepositEntity> findAllByOrderByIdAsc();
}
