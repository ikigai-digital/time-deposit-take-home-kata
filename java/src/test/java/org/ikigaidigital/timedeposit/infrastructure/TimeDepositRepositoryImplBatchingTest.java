package org.ikigaidigital.timedeposit.infrastructure;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import org.assertj.core.api.WithAssertions;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.ikigaidigital.timedeposit.TestcontainersConfiguration;
import org.ikigaidigital.timedeposit.infrastructure.entity.TimeDepositEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, TimeDepositRepositoryImpl.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TimeDepositRepositoryImplBatchingTest implements WithAssertions {

  private static final int CHUNK_SIZE = 50;

  @Autowired
  private TimeDepositRepositoryImpl timeDepositRepository;

  @Autowired
  private TimeDepositJpaRepository jpaRepository;

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  private Statistics statistics;

  @BeforeEach
  void setUp() {
    jpaRepository.deleteAllInBatch();
    jpaRepository.saveAllAndFlush(IntStream.rangeClosed(1, CHUNK_SIZE)
        .mapToObj(id -> new TimeDepositEntity(id, "basic", 45, new BigDecimal("1000.00")))
        .toList());
    statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    statistics.clear();
  }

  @Test
  void shouldWriteWholeChunkInOneJdbcBatch() {
    // given
    List<Integer> ids = IntStream.rangeClosed(1, CHUNK_SIZE).boxed().toList();

    // when
    timeDepositRepository.updateBalance(ids,
        deposits -> deposits.forEach(deposit -> deposit.setBalance(deposit.getBalance() + 1)));

    // then: SELECT ... FOR UPDATE + ONE batched UPDATE (without batching it would be 1 + 50)
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    assertThat(statistics.getEntityUpdateCount()).isEqualTo(CHUNK_SIZE);
    assertThat(jpaRepository.findAll()).extracting(TimeDepositEntity::getBalance)
        .allSatisfy(balance -> assertThat(balance).isEqualByComparingTo("1001.00"));
  }

  @Test
  void shouldNotWriteDepositsWhoseBalanceDidNotChange() {
    // given
    List<Integer> ids = IntStream.rangeClosed(1, CHUNK_SIZE).boxed().toList();

    // when: no interest earned
    timeDepositRepository.updateBalance(ids, deposits -> { });

    // then: only the SELECT; dirty checking finds nothing to write
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    assertThat(statistics.getEntityUpdateCount()).isZero();
  }
}