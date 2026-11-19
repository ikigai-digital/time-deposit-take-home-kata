package org.ikigaidigital.timedeposit.infrastructure;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.ikigaidigital.timedeposit.infrastructure.entity.BalanceUpdateProcessEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.enums.ProcessStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BalanceUpdateProcessJpaRepository extends
    JpaRepository<BalanceUpdateProcessEntity, Long> {

  @Transactional
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
    UPDATE BalanceUpdateProcessEntity p
    SET p.status = :status, p.finishedAt = :finishedAt
    WHERE p.id = :id
  """)
  void finish(@Param("id") long id,
      @Param("status") ProcessStatusEntity status,
      @Param("finishedAt") LocalDateTime finishedAt);

}
