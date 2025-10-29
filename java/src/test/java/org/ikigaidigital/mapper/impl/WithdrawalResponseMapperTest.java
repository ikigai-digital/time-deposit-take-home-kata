package org.ikigaidigital.mapper.impl;

import org.ikigaidigital.mapper.WithdrawalResponseMapper;
import org.ikigaidigital.web.domain.response.WithdrawalResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ikigaidigital.util.TestUtil.WITHDRAWAL_2;
import static org.ikigaidigital.util.TestUtil.WITHDRAWAL_RESPONSE_2;

class WithdrawalResponseMapperTest {

    private final WithdrawalResponseMapper testObj = new WithdrawalResponseMapperImpl();

    @Test
    void map() {
        WithdrawalResponse actualWithdrawalResponse = testObj.map(WITHDRAWAL_2);

        assertThat(actualWithdrawalResponse).isEqualTo(WITHDRAWAL_RESPONSE_2);
    }
}