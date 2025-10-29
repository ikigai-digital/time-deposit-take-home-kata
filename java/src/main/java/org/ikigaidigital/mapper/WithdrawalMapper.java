package org.ikigaidigital.mapper;

import org.ikigaidigital.entity.WithdrawalEntity;
import org.ikigaidigital.model.Withdrawal;

public interface WithdrawalMapper {

    Withdrawal map(final WithdrawalEntity withdrawalEntity);
}
