package org.ikigaidigital.mapper;

import org.ikigaidigital.mapper.impl.TimeDepositResponseMapperImpl;
import org.ikigaidigital.web.domain.response.TimeDepositResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ikigaidigital.util.TestUtil.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeDepositResponseMapperTest {

    @Mock
    private WithdrawalResponseMapper withdrawalResponseMapper;

    private TimeDepositResponseMapper testObj;

    @BeforeEach
    void setUp() {
        testObj = new TimeDepositResponseMapperImpl(withdrawalResponseMapper);
    }

    @Test
    public void map() {

        when(withdrawalResponseMapper.map(WITHDRAWAL_1))
                .thenReturn(WITHDRAWAL_RESPONSE_1);

        TimeDepositResponse actualTimeDepositResponse = testObj.map(TIME_DEPOSIT_WITH_WITHDRAWALS_1);

        assertThat(actualTimeDepositResponse).isEqualTo(TIME_DEPOSIT_RESPONSE_1);
    }

}