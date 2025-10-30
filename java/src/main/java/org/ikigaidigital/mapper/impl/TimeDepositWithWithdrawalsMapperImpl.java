package org.ikigaidigital.mapper.impl;

import lombok.AllArgsConstructor;
import org.ikigaidigital.entity.TimeDepositEntity;
import org.ikigaidigital.mapper.TimeDepositWithWithdrawalsMapper;
import org.ikigaidigital.mapper.WithdrawalMapper;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.model.Withdrawal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TimeDepositWithWithdrawalsMapperImpl implements TimeDepositWithWithdrawalsMapper {

    private WithdrawalMapper withdrawalMapper;

    @Override
    public TimeDepositWithWithdrawals map(TimeDepositEntity timeDepositEntity) {

        List<Withdrawal> widthdrawals = timeDepositEntity.getWithdrawals().stream()
                .map(withdrawalMapper::map).collect(Collectors.toList());

        return new TimeDepositWithWithdrawals(timeDepositEntity.getId(), timeDepositEntity.getPlanType(),
                timeDepositEntity.getBalance(), timeDepositEntity.getDays(), widthdrawals);
    }
}
