package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.application.port.out.WithdrawalRepository;
import org.ikigaidigital.domain.Withdrawal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TimeDepositServiceTest {

    private final InMemoryTimeDepositRepository timeDepositRepository = new InMemoryTimeDepositRepository();
    private final InMemoryWithdrawalRepository withdrawalRepository = new InMemoryWithdrawalRepository();
    private final TimeDepositService timeDepositService =
            new TimeDepositService(timeDepositRepository, withdrawalRepository, new TimeDepositCalculator());

    @Test
    @DisplayName("Should return every time deposit with its own withdrawals")
    void shouldReturnEveryTimeDepositWithItsOwnWithdrawals() {
        timeDepositRepository.storedTimeDeposits.add(new TimeDeposit(1, "basic", 1000.00, 45));
        timeDepositRepository.storedTimeDeposits.add(new TimeDeposit(2, "student", 2000.00, 100));
        Withdrawal firstWithdrawal = new Withdrawal(10, 1, new BigDecimal("100.00"), LocalDate.of(2026, 10, 1));
        Withdrawal secondWithdrawal = new Withdrawal(11, 1, new BigDecimal("50.00"), LocalDate.of(2026, 10, 2));
        withdrawalRepository.storedWithdrawals.addAll(List.of(firstWithdrawal, secondWithdrawal));

        List<TimeDepositWithWithdrawals> timeDeposits = timeDepositService.getAllTimeDeposits();

        assertThat(timeDeposits).hasSize(2);
        assertThat(timeDeposits.get(0).timeDeposit().getId()).isEqualTo(1);
        assertThat(timeDeposits.get(0).withdrawals()).containsExactly(firstWithdrawal, secondWithdrawal);
        assertThat(timeDeposits.get(1).timeDeposit().getId()).isEqualTo(2);
        assertThat(timeDeposits.get(1).withdrawals()).isEmpty();
    }

    @Test
    @DisplayName("Should save balances with one month of interest applied")
    void shouldSaveBalancesWithOneMonthOfInterestApplied() {
        timeDepositRepository.storedTimeDeposits.add(new TimeDeposit(1, "basic", 12000.00, 45));
        timeDepositRepository.storedTimeDeposits.add(new TimeDeposit(2, "premium", 12000.00, 40));

        timeDepositService.updateAllBalances();

        assertThat(timeDepositRepository.updatedTimeDeposits)
                .extracting(TimeDeposit::getId, TimeDeposit::getBalance)
                .containsExactly(tuple(1, 12010.00), tuple(2, 12000.00));
    }

    private static final class InMemoryTimeDepositRepository implements TimeDepositRepository {

        private final List<TimeDeposit> storedTimeDeposits = new ArrayList<>();
        private final List<TimeDeposit> updatedTimeDeposits = new ArrayList<>();

        @Override
        public List<TimeDeposit> findAll() {
            return List.copyOf(storedTimeDeposits);
        }

        @Override
        public void updateBalances(List<TimeDeposit> timeDeposits) {
            updatedTimeDeposits.addAll(timeDeposits);
        }
    }

    private static final class InMemoryWithdrawalRepository implements WithdrawalRepository {

        private final List<Withdrawal> storedWithdrawals = new ArrayList<>();

        @Override
        public List<Withdrawal> findAll() {
            return List.copyOf(storedWithdrawals);
        }
    }
}
