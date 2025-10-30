package org.ikigaidigital.repository;

import org.ikigaidigital.entity.TimeDepositEntity;
import org.ikigaidigital.entity.WithdrawalEntity;
import org.ikigaidigital.model.PlanType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
class TimeDepositRepositoryTest {

    @Autowired
    private TimeDepositRepository timeDepositRepository;

    @Test
    void getTimeDeposits() {

        List<TimeDepositEntity> actualTimeDeposits = timeDepositRepository.findAll();

        assertThat(actualTimeDeposits).hasSize(2);

        TimeDepositEntity actualFirstTimeDeposit = actualTimeDeposits.get(0);
        assertThat(actualFirstTimeDeposit.getId()).isEqualTo(1);
        assertThat(actualFirstTimeDeposit.getPlanType()).isEqualTo(PlanType.PREMIUM.name());
        assertThat(actualFirstTimeDeposit.getDays()).isEqualTo(365);
        assertThat(actualFirstTimeDeposit.getBalance()).isEqualTo(1250);

        List<WithdrawalEntity> actualWithdrawalsForFirstTimeDeposit = actualFirstTimeDeposit.getWithdrawals();
        assertThat(actualWithdrawalsForFirstTimeDeposit).hasSize(2);

        WithdrawalEntity actualFirstWithdrawalForFirstTimeDeposit = actualWithdrawalsForFirstTimeDeposit.get(0);
        assertThat(actualFirstWithdrawalForFirstTimeDeposit.getId()).isEqualTo(1);
        assertThat(actualFirstWithdrawalForFirstTimeDeposit.getAmount()).isEqualTo(10);
        assertThat(actualFirstWithdrawalForFirstTimeDeposit.getDate().getYear()).isEqualTo(2025);
        assertThat(actualFirstWithdrawalForFirstTimeDeposit.getDate().getMonthValue()).isEqualTo(9);
        assertThat(actualFirstWithdrawalForFirstTimeDeposit.getDate().getDayOfMonth()).isEqualTo(1);

        WithdrawalEntity actualSecondWithdrawalForFirstTimeDeposit = actualWithdrawalsForFirstTimeDeposit.get(1);
        assertThat(actualSecondWithdrawalForFirstTimeDeposit.getId()).isEqualTo(2);
        assertThat(actualSecondWithdrawalForFirstTimeDeposit.getAmount()).isEqualTo(20);
        assertThat(actualSecondWithdrawalForFirstTimeDeposit.getDate().getYear()).isEqualTo(2025);
        assertThat(actualSecondWithdrawalForFirstTimeDeposit.getDate().getMonthValue()).isEqualTo(10);
        assertThat(actualSecondWithdrawalForFirstTimeDeposit.getDate().getDayOfMonth()).isEqualTo(1);

        TimeDepositEntity actualSecondTimeDeposit = actualTimeDeposits.get(1);
        assertThat(actualSecondTimeDeposit.getId()).isEqualTo(2);
        assertThat(actualSecondTimeDeposit.getPlanType()).isEqualTo(PlanType.STUDENT.name());
        assertThat(actualSecondTimeDeposit.getDays()).isEqualTo(730);
        assertThat(actualSecondTimeDeposit.getBalance()).isEqualTo(5000);

        List<WithdrawalEntity> actualWithdrawalsForSecondTimeDeposit = actualSecondTimeDeposit.getWithdrawals();
        assertThat(actualWithdrawalsForSecondTimeDeposit).hasSize(1);

        WithdrawalEntity actualFirstWithdrawalForSecondTimeDeposit = actualWithdrawalsForSecondTimeDeposit.get(0);
        assertThat(actualFirstWithdrawalForSecondTimeDeposit.getId()).isEqualTo(3);
        assertThat(actualFirstWithdrawalForSecondTimeDeposit.getAmount()).isEqualTo(20);
        assertThat(actualFirstWithdrawalForSecondTimeDeposit.getDate().getYear()).isEqualTo(2024);
        assertThat(actualFirstWithdrawalForSecondTimeDeposit.getDate().getMonthValue()).isEqualTo(6);
        assertThat(actualFirstWithdrawalForSecondTimeDeposit.getDate().getDayOfMonth()).isEqualTo(25);
    }
}