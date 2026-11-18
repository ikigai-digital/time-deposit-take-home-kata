package org.ikigaidigital.timedeposit.domain;

import java.util.Optional;

public interface BalanceUpdateService {

  Optional<Long> startUpdate();
}
