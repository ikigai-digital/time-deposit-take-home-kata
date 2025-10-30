package org.ikigaidigital.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import org.ikigaidigital.entity.TimeDepositEntity;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_ENTITY_1;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_ENTITY_2;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_WITH_WITHDRAWALS_1;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_WITH_WITHDRAWALS_2;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeDepositServiceImplTest {

    @Mock
    private TimeDepositWithWithdrawalsMapper timeDepositWithWithdrawalsMapper;

    @Mock
    private TimeDepositRepository timeDepositRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonPatch patchJson;

    @Mock
    private JsonNode jsonNode;

    private TimeDepositService timeDepositService;

    @BeforeEach
    void setUp() {
        timeDepositService
                = new TimeDepositServiceImpl(timeDepositRepository, timeDepositWithWithdrawalsMapper, objectMapper);
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
    void update_successfully() throws Exception {

        when(timeDepositRepository.findById(1))
                .thenReturn(java.util.Optional.of(TIME_DEPOSIT_ENTITY_1));
        when(objectMapper.convertValue(TIME_DEPOSIT_ENTITY_1, JsonNode.class)).thenReturn(jsonNode);
        when(patchJson.apply(any(JsonNode.class))).thenReturn(jsonNode);
        when(objectMapper.treeToValue(jsonNode, TimeDepositEntity.class)).thenReturn(TIME_DEPOSIT_ENTITY_1);
        when(timeDepositRepository.saveAndFlush(TIME_DEPOSIT_ENTITY_1)).thenReturn(TIME_DEPOSIT_ENTITY_1);
        when(timeDepositWithWithdrawalsMapper.map(TIME_DEPOSIT_ENTITY_1)).thenReturn(TIME_DEPOSIT_WITH_WITHDRAWALS_1);

        TimeDepositWithWithdrawals actualTimeDepositWithWithdrawals = timeDepositService.update(1, patchJson);

        assertThat(actualTimeDepositWithWithdrawals).isEqualTo(TIME_DEPOSIT_WITH_WITHDRAWALS_1);
    }

    @Test
    void update_exceptionDuringTimeDepositFind_throwsTimeDepositNotFoundException() {

        when(timeDepositRepository.findById(1)).thenThrow(new RuntimeException("test_error"));

        assertThatThrownBy(() -> timeDepositService.update(1, patchJson))
                .isExactlyInstanceOf(TimeDepositNotFoundException.class)
                .hasMessage("Time deposit with id: 1 not found");
    }

    @Test
    void update_noDepositFound_throwsTimeDepositNotFoundException() {

        when(timeDepositRepository.findById(1)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> timeDepositService.update(1, patchJson))
                .isExactlyInstanceOf(TimeDepositNotFoundException.class)
                .hasMessage("Time deposit with id: 1 not found");
    }

    @Test
    void update_exceptionDuringUpdate_throwsInternalServerException() throws Exception {

        when(timeDepositRepository.findById(1))
                .thenReturn(java.util.Optional.of(TIME_DEPOSIT_ENTITY_1));
        when(objectMapper.convertValue(TIME_DEPOSIT_ENTITY_1, JsonNode.class)).thenReturn(jsonNode);
        when(patchJson.apply(any(JsonNode.class))).thenReturn(jsonNode);
        when(objectMapper.treeToValue(jsonNode, TimeDepositEntity.class)).thenReturn(TIME_DEPOSIT_ENTITY_1);
        when(timeDepositRepository.saveAndFlush(TIME_DEPOSIT_ENTITY_1)).thenThrow(new RuntimeException("test_error"));

        assertThatThrownBy(() -> timeDepositService.update(1, patchJson))
                .isExactlyInstanceOf(InternalServerErrorException.class)
                .hasMessage("Error while updating time deposit with id: 1");
    }

}