package org.ikigaidigital.timedeposit.domain;

import java.util.List;
import java.util.function.Consumer;
import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;

public interface TimeDepositRepository {

  List<Integer> getTimeDepositIds();

  void updateBalance(List<Integer> ids, Consumer<List<TimeDeposit>> balanceUpdater);
}
