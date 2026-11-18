package org.ikigaidigital.timedeposit.domain;

import org.ikigaidigital.timedeposit.domain.model.PageResult;
import org.ikigaidigital.timedeposit.domain.model.TimeDepositDetails;

public interface TimeDepositQueryService {

  PageResult<TimeDepositDetails> getTimeDeposits(int page, int size);
}
