package org.ikigaidigital.mapper.impl;

import org.ikigaidigital.mapper.TimeDepositResponseMapper;
import org.ikigaidigital.mapper.WithdrawalResponseMapper;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.web.domain.response.TimeDepositResponse;
import org.ikigaidigital.web.domain.response.WithdrawalResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TimeDepositResponseMapperImpl implements TimeDepositResponseMapper {

    private WithdrawalResponseMapper withdrawalResponseMapper;

    public TimeDepositResponseMapperImpl(WithdrawalResponseMapper withdrawalResponseMapper) {
        this.withdrawalResponseMapper = withdrawalResponseMapper;
    }

    @Override
    public TimeDepositResponse map(TimeDepositWithWithdrawals timeDeposit) {

        List<WithdrawalResponse> withdrawals = timeDeposit.getWithdrawals().stream()
                .map(withdrawal -> withdrawalResponseMapper.map(withdrawal)).toList();
        return new TimeDepositResponse(timeDeposit.getId(), timeDeposit.getPlanType(),
                timeDeposit.getBalance(), timeDeposit.getDays(), withdrawals);
    }
}
