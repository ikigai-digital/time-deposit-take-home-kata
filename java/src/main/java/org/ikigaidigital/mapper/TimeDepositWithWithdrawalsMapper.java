package org.ikigaidigital.mapper;

import org.ikigaidigital.entity.TimeDepositEntity;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;

public interface TimeDepositWithWithdrawalsMapper {

    TimeDepositWithWithdrawals map(final TimeDepositEntity timeDepositEntity);
}
