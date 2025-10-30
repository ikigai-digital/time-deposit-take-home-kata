package org.ikigaidigital.controller;

import org.ikigaidigital.model.PlanType;
import org.ikigaidigital.web.domain.response.TimeDepositResponse;
import org.ikigaidigital.web.domain.response.WithdrawalResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TimeDepositEndpointsTest {

    private static final String GET_DEPOSITS = "/deposits";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getTimeDeposits() {

        ResponseEntity<TimeDepositResponse[]> actualResponse = this.restTemplate
                .getForEntity("http://localhost:" + port + GET_DEPOSITS, TimeDepositResponse[].class);

        assertThat(actualResponse.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

        TimeDepositResponse[] actualTimeDeposits = actualResponse.getBody();

        assertThat(actualTimeDeposits).hasSize(2);

        TimeDepositResponse firstActualTimeDeposit = actualTimeDeposits[0];
        assertThat(firstActualTimeDeposit.getBalance()).isEqualTo(1250);
        assertThat(firstActualTimeDeposit.getPlanType()).isEqualTo(PlanType.PREMIUM.name());
        assertThat(firstActualTimeDeposit.getDays()).isEqualTo(365);

        List<WithdrawalResponse> withdrawalsForFirstTimeDeposit = firstActualTimeDeposit.getWithdrawals();
        assertThat(withdrawalsForFirstTimeDeposit).hasSize(2);

        WithdrawalResponse firstWithdrawalForFirstTimeDeposit = withdrawalsForFirstTimeDeposit.get(0);
        assertThat(firstWithdrawalForFirstTimeDeposit.getAmount()).isEqualTo(10);
        assertThat(firstWithdrawalForFirstTimeDeposit.getDate().getYear()).isEqualTo(2025);
        assertThat(firstWithdrawalForFirstTimeDeposit.getDate().getMonthValue()).isEqualTo(9);
        assertThat(firstWithdrawalForFirstTimeDeposit.getDate().getDayOfMonth()).isEqualTo(1);

        WithdrawalResponse secondWithdrawalForFirstTimeDeposit = withdrawalsForFirstTimeDeposit.get(1);
        assertThat(secondWithdrawalForFirstTimeDeposit.getAmount()).isEqualTo(20);
        assertThat(secondWithdrawalForFirstTimeDeposit.getDate().getYear()).isEqualTo(2025);
        assertThat(secondWithdrawalForFirstTimeDeposit.getDate().getMonthValue()).isEqualTo(10);
        assertThat(secondWithdrawalForFirstTimeDeposit.getDate().getDayOfMonth()).isEqualTo(1);

        TimeDepositResponse secondActualTimeDeposit = actualTimeDeposits[1];
        assertThat(secondActualTimeDeposit.getBalance()).isEqualTo(5000);
        assertThat(secondActualTimeDeposit.getPlanType()).isEqualTo(PlanType.STUDENT.name());
        assertThat(secondActualTimeDeposit.getDays()).isEqualTo(730);

        List<WithdrawalResponse> withdrawalsForSecondTimeDeposit = secondActualTimeDeposit.getWithdrawals();
        assertThat(withdrawalsForSecondTimeDeposit).hasSize(1);

        WithdrawalResponse firstWithdrawalForSecondTimeDeposit = withdrawalsForSecondTimeDeposit.get(0);
        assertThat(firstWithdrawalForSecondTimeDeposit.getAmount()).isEqualTo(20);
        assertThat(firstWithdrawalForSecondTimeDeposit.getDate().getYear()).isEqualTo(2024);
        assertThat(firstWithdrawalForSecondTimeDeposit.getDate().getMonthValue()).isEqualTo(6);
        assertThat(firstWithdrawalForSecondTimeDeposit.getDate().getDayOfMonth()).isEqualTo(25);
    }
}
