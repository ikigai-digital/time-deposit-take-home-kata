package org.ikigaidigital.timedeposit.api.mapper;

import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.ikigaidigital.timedeposit.api.dto.PageDto;
import org.ikigaidigital.timedeposit.api.dto.TimeDepositDto;
import org.ikigaidigital.timedeposit.api.dto.WithdrawalDto;
import org.ikigaidigital.timedeposit.domain.model.PageResult;
import org.ikigaidigital.timedeposit.domain.model.TimeDepositDetails;
import org.ikigaidigital.timedeposit.domain.model.Withdrawal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeDepositDtoMapper {

  public static PageDto<TimeDepositDto> toPageDto(PageResult<TimeDepositDetails> page) {
    return toPageDto(page, TimeDepositDtoMapper::toTimeDepositDto);
  }

  private static TimeDepositDto toTimeDepositDto(TimeDepositDetails deposit) {
    var withdrawals = deposit.withdrawals().stream()
        .map(TimeDepositDtoMapper::toWithdrawalDto)
        .toList();
    return new TimeDepositDto(deposit.id(), deposit.planType(), deposit.balance(), deposit.days(),
        withdrawals);
  }

  private static WithdrawalDto toWithdrawalDto(Withdrawal withdrawal) {
    return new WithdrawalDto(withdrawal.id(), withdrawal.amount(), withdrawal.date());
  }

  private static <T, R> PageDto<R> toPageDto(PageResult<T> page, Function<T, R> mapper) {
    var content = page.content().stream().map(mapper).toList();
    return new PageDto<>(content, page.page(), page.size(), page.totalElements(), page.totalPages());
  }
}
