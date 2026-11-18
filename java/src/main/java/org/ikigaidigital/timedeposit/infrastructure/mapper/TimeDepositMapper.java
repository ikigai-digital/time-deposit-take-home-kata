package org.ikigaidigital.timedeposit.infrastructure.mapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;
import org.ikigaidigital.timedeposit.domain.model.TimeDepositDetails;
import org.ikigaidigital.timedeposit.domain.model.Withdrawal;
import org.ikigaidigital.timedeposit.infrastructure.entity.TimeDepositEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.projection.TimeDepositWithdrawalRow;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeDepositMapper {

	public static TimeDeposit toDomain(TimeDepositEntity entity) {
		return new TimeDeposit(entity.getId(), entity.getPlanType(), entity.getBalance().doubleValue(), entity.getDays());
	}

  public static List<TimeDepositDetails> toDetails(List<TimeDepositWithdrawalRow> rows) {
    return rows.stream()
        .collect(Collectors.groupingBy(TimeDepositWithdrawalRow::depositId, LinkedHashMap::new,
            Collectors.toList()))
        .values().stream()
        .map(TimeDepositMapper::toDetailsOfOneDeposit)
        .toList();
  }

  private static TimeDepositDetails toDetailsOfOneDeposit(List<TimeDepositWithdrawalRow> rowsOfDeposit) {
    var deposit = rowsOfDeposit.getFirst();
    var withdrawals = rowsOfDeposit.stream()
        .filter(TimeDepositWithdrawalRow::hasWithdrawal)
        .map(row -> new Withdrawal(row.withdrawalId(), row.amount(), row.date()))
        .toList();
    return new TimeDepositDetails(deposit.depositId(), deposit.planType(), deposit.balance(), deposit.days(),
        withdrawals);
  }

}
