package org.ikigaidigital.service.impl;

import org.ikigaidigital.exception.InternalServerErrorException;
import org.ikigaidigital.exception.TimeDepositNotFoundException;
import org.ikigaidigital.mapper.TimeDepositWithWithdrawalsMapper;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.repository.TimeDepositRepository;
import org.ikigaidigital.service.TimeDepositService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_ENTITY_1;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_ENTITY_2;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_WITH_WITHDRAWALS_1;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_WITH_WITHDRAWALS_2;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeDepositServiceImplTest {

    @Mock
    private TimeDepositWithWithdrawalsMapper timeDepositWithWithdrawalsMapper;

    @Mock
    private TimeDepositRepository timeDepositRepository;

    private TimeDepositService timeDepositService;

    @BeforeEach
    void setUp() {
        timeDepositService
                = new TimeDepositServiceImpl(timeDepositRepository, timeDepositWithWithdrawalsMapper);
    }

    @Test
    void getTimeDeposits_noTimeDeposits_returnsEmptyList() {

        when(timeDepositRepository.findAll()).thenReturn(List.of());

        List<TimeDepositWithWithdrawals> actualTimeDeposits = timeDepositService.getTimeDeposits();

        assertThat(actualTimeDeposits).isEmpty();
    }

    @Test
    void getTimeDeposits() {

        when(timeDepositRepository.findAll()).thenReturn(List.of(TIME_DEPOSIT_ENTITY_1, TIME_DEPOSIT_ENTITY_2));
        when(timeDepositWithWithdrawalsMapper.map(TIME_DEPOSIT_ENTITY_1)).thenReturn(TIME_DEPOSIT_WITH_WITHDRAWALS_1);
        when(timeDepositWithWithdrawalsMapper.map(TIME_DEPOSIT_ENTITY_2)).thenReturn(TIME_DEPOSIT_WITH_WITHDRAWALS_2);

        List<TimeDepositWithWithdrawals> actualTimeDeposits = timeDepositService.getTimeDeposits();

        assertThat(actualTimeDeposits)
                .containsExactlyInAnyOrder(TIME_DEPOSIT_WITH_WITHDRAWALS_1, TIME_DEPOSIT_WITH_WITHDRAWALS_2);
    }

    @Test
    void getTimeDeposits_throwsException_shouldPropagateException() {

        when(timeDepositRepository.findAll()).thenThrow(new RuntimeException("test_error"));

        assertThatThrownBy(() -> timeDepositService.getTimeDeposits())
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("test_error");
    }

    @Test
    void update_successfully() {

        when(timeDepositRepository.findById(1))
                .thenReturn(java.util.Optional.of(TIME_DEPOSIT_ENTITY_1));
        when(timeDepositRepository.saveAndFlush(TIME_DEPOSIT_ENTITY_1)).thenReturn(TIME_DEPOSIT_ENTITY_1);
        when(timeDepositWithWithdrawalsMapper.map(TIME_DEPOSIT_ENTITY_1)).thenReturn(TIME_DEPOSIT_WITH_WITHDRAWALS_1);

        TimeDepositWithWithdrawals actual = timeDepositService.update(1, Map.of("balance", 1500.60));
        assertThat(actual).isEqualTo(TIME_DEPOSIT_WITH_WITHDRAWALS_1);
    }

    @Test
    void update_exceptionDuringTimeDepositFind_throwsTimeDepositNotFoundException() {

        when(timeDepositRepository.findById(1)).thenThrow(new RuntimeException("test_error"));

        assertThatThrownBy(() -> timeDepositService.update(1, Map.of("balance", 1500.60)))
                .isExactlyInstanceOf(TimeDepositNotFoundException.class)
                .hasMessage("Time deposit with id: 1 not found");
    }

    @Test
    void update_noDepositFound_throwsTimeDepositNotFoundException() {

        when(timeDepositRepository.findById(1)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> timeDepositService.update(1, Map.of("balance", 1500.60)))
                .isExactlyInstanceOf(TimeDepositNotFoundException.class)
                .hasMessage("Time deposit with id: 1 not found");
    }

    @Test
    void update_exceptionDuringUpdate_throwsInternalServerException() throws Exception {

        when(timeDepositRepository.findById(1))
                .thenReturn(java.util.Optional.of(TIME_DEPOSIT_ENTITY_1));
        when(timeDepositRepository.saveAndFlush(TIME_DEPOSIT_ENTITY_1)).thenThrow(new RuntimeException("test_error"));

        assertThatThrownBy(() -> timeDepositService.update(1, Map.of("balance", 1500.60)))
                .isExactlyInstanceOf(InternalServerErrorException.class)
                .hasMessage("Error while updating time deposit with id: 1");
    }

}