package org.ikigaidigital.timedeposit.domain;

import lombok.RequiredArgsConstructor;
import org.ikigaidigital.timedeposit.domain.model.PageResult;
import org.ikigaidigital.timedeposit.domain.model.TimeDepositDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeDepositQueryServiceImpl implements TimeDepositQueryService {

  private final TimeDepositRepository timeDepositRepository;

  @Override
  public PageResult<TimeDepositDetails> getTimeDeposits(int page, int size) {
    return timeDepositRepository.findPage(page, size);
  }
}
