package org.ikigaidigital.mapper.impl;

import org.ikigaidigital.entity.WithdrawalEntity;
import org.ikigaidigital.mapper.WithdrawalMapper;
import org.ikigaidigital.model.Withdrawal;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalMapperImpl implements WithdrawalMapper {

    @Override
    public Withdrawal map(WithdrawalEntity withdrawalEntity) {
        return new Withdrawal(withdrawalEntity.getAmount(), withdrawalEntity.getDate());
    }
}
