package org.ikigaidigital.timedeposit.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ikigaidigital.timedeposit.infrastructure.entity.enums.ProcessStatusEntity;

@Entity
@Table(name = "balance_update_processes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateProcessEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ProcessStatusEntity status;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "finished_at")
  private LocalDateTime finishedAt;

  public static BalanceUpdateProcessEntity inProgress() {
    BalanceUpdateProcessEntity process = new BalanceUpdateProcessEntity();
    process.status = ProcessStatusEntity.IN_PROGRESS;
    process.startedAt = LocalDateTime.now();
    return process;
  }
}
