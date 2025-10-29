package org.ikigaidigital.mapper.impl;

import org.ikigaidigital.mapper.TimeDepositWithWithdrawalsMapper;
import org.ikigaidigital.mapper.WithdrawalMapper;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_ENTITY_1;
import static org.ikigaidigital.util.TestUtil.TIME_DEPOSIT_WITH_WITHDRAWALS_1;
import static org.ikigaidigital.util.TestUtil.WITHDRAWAL_1;
import static org.ikigaidigital.util.TestUtil.WITHDRAWAL_ENTITY_1;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeDepositWithWithdrawalsMapperTest {

    @Mock
    private WithdrawalMapper withdrawalMapper;

    private TimeDepositWithWithdrawalsMapper testObj;

    @BeforeEach
    void setUp() {
        testObj = new TimeDepositWithWithdrawalsMapperImpl(withdrawalMapper);
    }

    @Test
    void map_successfully() {

        when(withdrawalMapper.map(WITHDRAWAL_ENTITY_1)).thenReturn(WITHDRAWAL_1);

        TimeDepositWithWithdrawals actual = testObj.map(TIME_DEPOSIT_ENTITY_1);

        assertThat(actual).isEqualTo(TIME_DEPOSIT_WITH_WITHDRAWALS_1);
    }
}