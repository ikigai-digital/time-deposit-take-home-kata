package org.ikigaidigital.timedeposit.infrastructure;

import jakarta.persistence.LockModeType;
import java.util.List;
import org.ikigaidigital.timedeposit.infrastructure.entity.TimeDepositEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.projection.TimeDepositWithdrawalRow;
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

  @Query("""
      SELECT new org.ikigaidigital.timedeposit.infrastructure.entity.projection.TimeDepositWithdrawalRow(
          t.id, t.planType, t.balance, t.days, w.id, w.amount, w.date)
      FROM TimeDepositEntity t
      LEFT JOIN WithdrawalEntity w ON w.timeDepositId = t.id
      WHERE t.id IN (
          SELECT pr.id FROM (
              SELECT d.id AS id, row_number() OVER (ORDER BY d.id) AS rowNumber
              FROM TimeDepositEntity d
          ) pr
          WHERE pr.rowNumber > :fromRow AND pr.rowNumber <= :toRow
      )
      ORDER BY t.id, w.date, w.id
      """)
  List<TimeDepositWithdrawalRow> findPageRows(@Param("fromRow") long fromRow, @Param("toRow") long toRow);
}
