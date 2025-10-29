package org.ikigaidigital.mapper.impl;

import org.ikigaidigital.mapper.WithdrawalResponseMapper;
import org.ikigaidigital.model.Withdrawal;
import org.ikigaidigital.web.domain.response.WithdrawalResponse;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalResponseMapperImpl implements WithdrawalResponseMapper {

    @Override
    public WithdrawalResponse map(final Withdrawal withdrawal) {
        return new WithdrawalResponse(withdrawal.getAmount(), withdrawal.getDate());
    }
}
