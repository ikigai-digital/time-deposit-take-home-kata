package org.ikigaidigital.timedeposit.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ikigaidigital.timedeposit.config.BalanceUpdateProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceUpdateServiceImpl implements BalanceUpdateService {

  private final TimeDepositRepository timeDepositRepository;
  private final BalanceUpdateProcessRepository processes;
  private final TimeDepositCalculator calculator;
  private final ExecutorService balanceUpdateChunkExecutor;
  private final BalanceUpdateProperties properties;

  @Override
  public Optional<Long> startUpdate() {
    Optional<Long> processId = processes.tryStart();
    processId.ifPresent(id -> Thread.ofVirtual().name("balance-update-" + id).start(() -> run(id)));

    return processId;
  }

  private void run(long processId) {
    var chunkSize = properties.chunkSize();
    try {
      var timeDepositIds = timeDepositRepository.getTimeDepositIds();
      var chunks = new ArrayList<CompletableFuture<Void>>();
      for (var i = 0; i < timeDepositIds.size(); i += chunkSize) {
        var chunkIds = List.copyOf(
            timeDepositIds.subList(i, Math.min(i + chunkSize, timeDepositIds.size())));
        chunks.add(CompletableFuture.runAsync(
            () -> timeDepositRepository.updateBalance(chunkIds, calculator::updateBalance),
            balanceUpdateChunkExecutor));
      }
      CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
      processes.markCompleted(processId);
      log.info("Interest update process {} completed", processId);
    } catch (Exception e) {
      processes.markFailed(processId);
      log.error("Interest update process {} failed", processId, e);
    }
  }
}
