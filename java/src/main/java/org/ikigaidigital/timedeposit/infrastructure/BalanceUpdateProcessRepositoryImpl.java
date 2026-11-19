package org.ikigaidigital.timedeposit.infrastructure;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ikigaidigital.timedeposit.domain.BalanceUpdateProcessRepository;
import org.ikigaidigital.timedeposit.infrastructure.entity.BalanceUpdateProcessEntity;
import org.ikigaidigital.timedeposit.infrastructure.entity.enums.ProcessStatusEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceUpdateProcessRepositoryImpl implements BalanceUpdateProcessRepository {

  private final BalanceUpdateProcessJpaRepository repository;

  @Override
  public Optional<Long> tryStart() {
    try {
      return Optional.of(repository.saveAndFlush(BalanceUpdateProcessEntity.inProgress()).getId());
    } catch (DataIntegrityViolationException alreadyRunning) {
      return Optional.empty();
    }
  }

  @Override
  public void markCompleted(long processId) {
    repository.finish(processId, ProcessStatusEntity.COMPLETED, LocalDateTime.now());
  }

  @Override
  public void markFailed(long processId) {
    repository.finish(processId, ProcessStatusEntity.FAILED, LocalDateTime.now());
  }
}
