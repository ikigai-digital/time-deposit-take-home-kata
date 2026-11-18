package org.ikigaidigital.timedeposit.infrastructure;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.ikigaidigital.timedeposit.domain.model.TimeDeposit;
import org.ikigaidigital.timedeposit.domain.TimeDepositRepository;
import org.ikigaidigital.timedeposit.infrastructure.mapper.TimeDepositMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TimeDepositRepositoryImpl implements TimeDepositRepository {

  private final TimeDepositJpaRepository repository;

  @Override
  public List<Integer> getTimeDepositIds() {
    return repository.findAllIds();
  }

  @Transactional
  @Override
  public void updateBalance(List<Integer> ids, Consumer<List<TimeDeposit>> balanceUpdater) {
    var depositEntities = repository.findAllByIdForUpdate(ids);
    var deposits = depositEntities.stream()
        .map(TimeDepositMapper::toDomain)
        .toList();

    balanceUpdater.accept(deposits);

    for (var i = 0; i < depositEntities.size(); i++) {
      depositEntities.get(i).setBalance(BigDecimal.valueOf(deposits.get(i).getBalance()));
    }
  }
}
