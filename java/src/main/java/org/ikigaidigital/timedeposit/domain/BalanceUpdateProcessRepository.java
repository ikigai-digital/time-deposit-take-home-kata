package org.ikigaidigital.timedeposit.domain;

import java.util.Optional;

public interface BalanceUpdateProcessRepository {

  Optional<Long> tryStart();

  void markCompleted(long processId);

  void markFailed(long processId);
}
