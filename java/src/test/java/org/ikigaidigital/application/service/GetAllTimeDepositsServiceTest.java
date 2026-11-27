package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.application.port.in.TimeDepositDetails;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetAllTimeDepositsServiceTest {

    private static final Withdrawal FIRST = withdrawal(1, 1, "500.00", "2026-01-15");
    private static final Withdrawal SECOND = withdrawal(2, 1, "250.50", "2026-03-02");
    private static final Withdrawal OTHER_DEPOSIT = withdrawal(3, 3, "100.00", "2026-02-20");

    @Test
    void returnsEveryDepositWithItsOwnWithdrawals() {
        GetAllTimeDepositsService service = new GetAllTimeDepositsService(
                new InMemoryTimeDepositRepository(
                        new TimeDeposit(1, "basic", 10_000.00, 45),
                        new TimeDeposit(2, "student", 5_000.00, 400),
                        new TimeDeposit(3, "premium", 20_000.00, 90)),
                new InMemoryWithdrawalRepository(FIRST, SECOND, OTHER_DEPOSIT));

        List<TimeDepositDetails> result = service.getAllTimeDeposits();

        assertThat(result).extracting(details -> details.timeDeposit().getId()).containsExactly(1, 2, 3);
        assertThat(result.get(0).withdrawals()).containsExactly(FIRST, SECOND);
        assertThat(result.get(1).withdrawals()).isEmpty();
        assertThat(result.get(2).withdrawals()).containsExactly(OTHER_DEPOSIT);
    }

    @Test
    void returnsDepositFieldsAsStored() {
        GetAllTimeDepositsService service = new GetAllTimeDepositsService(
                new InMemoryTimeDepositRepository(new TimeDeposit(7, "gold", 1_000.00, 100)),
                new InMemoryWithdrawalRepository());

        TimeDeposit deposit = service.getAllTimeDeposits().get(0).timeDeposit();

        assertThat(deposit.getId()).isEqualTo(7);
        assertThat(deposit.getPlanType()).isEqualTo("gold");
        assertThat(deposit.getBalance()).isEqualTo(1_000.00);
        assertThat(deposit.getDays()).isEqualTo(100);
    }

    @Test
    void doesNotApplyInterest() {
        InMemoryTimeDepositRepository repository =
                new InMemoryTimeDepositRepository(new TimeDeposit(1, "basic", 10_000.00, 45));
        GetAllTimeDepositsService service = new GetAllTimeDepositsService(repository, new InMemoryWithdrawalRepository());

        service.getAllTimeDeposits();

        assertThat(repository.get(1).getBalance()).isEqualTo(10_000.00);
    }

    @Test
    void returnsEmptyListWhenThereAreNoDeposits() {
        GetAllTimeDepositsService service = new GetAllTimeDepositsService(
                new InMemoryTimeDepositRepository(), new InMemoryWithdrawalRepository());

        assertThat(service.getAllTimeDeposits()).isEmpty();
    }

    private static Withdrawal withdrawal(int id, int timeDepositId, String amount, String date) {
        return new Withdrawal(id, timeDepositId, new BigDecimal(amount), LocalDate.parse(date));
    }
}
