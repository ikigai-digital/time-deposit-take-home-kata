package org.ikigaidigital.mapper.impl;

import org.ikigaidigital.model.Withdrawal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.ikigaidigital.util.TestUtil.WITHDRAWAL_1;
import static org.ikigaidigital.util.TestUtil.WITHDRAWAL_ENTITY_1;

class WithdrawalMapperImplTest {

    private final WithdrawalMapperImpl testObj = new WithdrawalMapperImpl();

    @Test
    void map_successfully() {
        Withdrawal actual = testObj.map(WITHDRAWAL_ENTITY_1);

        assertThat(actual).isEqualTo(WITHDRAWAL_1);
    }

}