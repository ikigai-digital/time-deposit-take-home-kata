package org.ikigaidigital.mapper;

import org.ikigaidigital.model.Withdrawal;
import org.ikigaidigital.web.domain.response.WithdrawalResponse;

public interface WithdrawalResponseMapper {

    WithdrawalResponse map(final Withdrawal withdrawal);
}
