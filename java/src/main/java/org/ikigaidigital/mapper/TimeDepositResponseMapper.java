package org.ikigaidigital.mapper;

import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.web.domain.response.TimeDepositResponse;

public interface TimeDepositResponseMapper {

    TimeDepositResponse map(final TimeDepositWithWithdrawals timeDeposit);
}
