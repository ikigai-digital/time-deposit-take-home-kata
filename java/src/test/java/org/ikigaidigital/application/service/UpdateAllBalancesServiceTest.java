package org.ikigaidigital.application.service;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateAllBalancesServiceTest {

    @Test
    void appliesMonthlyInterestToEveryStoredDepositAndSavesIt() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository(
                new TimeDeposit(1, "basic", 10_000.00, 45),
                new TimeDeposit(2, "student", 5_000.00, 200),
                new TimeDeposit(3, "premium", 20_000.00, 90));

        service(repository).updateAllBalances();

        assertThat(repository.get(1).getBalance()).isEqualTo(10_008.33);
        assertThat(repository.get(2).getBalance()).isEqualTo(5_012.50);
        assertThat(repository.get(3).getBalance()).isEqualTo(20_083.33);
    }

    @Test
    void leavesDepositsNotEarningInterestUnchanged() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository(
                new TimeDeposit(1, "basic", 10_000.00, 30),
                new TimeDeposit(2, "student", 5_000.00, 400),
                new TimeDeposit(3, "premium", 20_000.00, 45),
                new TimeDeposit(4, "gold", 1_000.00, 100));

        service(repository).updateAllBalances();

        assertThat(repository.get(1).getBalance()).isEqualTo(10_000.00);
        assertThat(repository.get(2).getBalance()).isEqualTo(5_000.00);
        assertThat(repository.get(3).getBalance()).isEqualTo(20_000.00);
        assertThat(repository.get(4).getBalance()).isEqualTo(1_000.00);
    }

    @Test
    void doesNotChangeAnythingButTheBalance() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository(
                new TimeDeposit(1, "premium", 20_000.00, 90));

        service(repository).updateAllBalances();

        TimeDeposit saved = repository.get(1);
        assertThat(saved.getPlanType()).isEqualTo("premium");
        assertThat(saved.getDays()).isEqualTo(90);
    }

    @Test
    void eachCallAccruesAnotherMonth() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository(
                new TimeDeposit(1, "premium", 10_000.00, 100));
        UpdateAllBalancesService service = service(repository);

        service.updateAllBalances();
        service.updateAllBalances();

        assertThat(repository.get(1).getBalance()).isEqualTo(10_083.51);
    }

    @Test
    void handlesEmptyRepository() {
        InMemoryTimeDepositRepository repository = new InMemoryTimeDepositRepository();

        service(repository).updateAllBalances();

        assertThat(repository.findAll()).isEmpty();
    }

    private static UpdateAllBalancesService service(InMemoryTimeDepositRepository repository) {
        return new UpdateAllBalancesService(repository, new TimeDepositCalculator());
    }
}
