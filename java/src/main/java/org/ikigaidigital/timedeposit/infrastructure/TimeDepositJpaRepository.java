package org.ikigaidigital.timedeposit.infrastructure;

import jakarta.persistence.LockModeType;
import java.util.List;
import org.ikigaidigital.timedeposit.infrastructure.entity.TimeDepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TimeDepositJpaRepository extends JpaRepository<TimeDepositEntity, Integer> {

  @Query("SELECT t.id FROM TimeDepositEntity t ORDER BY t.id")
  List<Integer> findAllIds();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT t FROM TimeDepositEntity t WHERE t.id IN :ids ORDER BY t.id")
  List<TimeDepositEntity> findAllByIdForUpdate(@Param("ids") List<Integer> ids);
}
