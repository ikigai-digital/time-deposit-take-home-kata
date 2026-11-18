package org.ikigaidigital.timedeposit.domain;

import java.util.List;
import java.util.function.Consumer;
import org.ikigaidigital.timedeposit.domain.model.PageResult;
import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;
import org.ikigaidigital.timedeposit.domain.model.TimeDepositDetails;

public interface TimeDepositRepository {

  List<Integer> getTimeDepositIds();

  void updateBalance(List<Integer> ids, Consumer<List<TimeDeposit>> balanceUpdater);

  PageResult<TimeDepositDetails> findPage(int page, int size);
}
