package org.ikigaidigital.util;

import org.ikigaidigital.entity.TimeDepositEntity;
import org.ikigaidigital.model.PlanType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.ikigaidigital.util.TestUtil.WITHDRAWAL_ENTITY_1;

class PropertiesUtilTest {

    private final static Double NEW_BALANCE = 1500.8;

    @Test
    void updateProperty() {

        TimeDepositEntity timeDepositEntity
                = new TimeDepositEntity(2, PlanType.PREMIUM.name(), 200d, 770, List.of(WITHDRAWAL_ENTITY_1));

        PropertiesUtil.updateProperty(timeDepositEntity, "balance", NEW_BALANCE);

        assertThat(timeDepositEntity.getBalance()).isEqualTo(NEW_BALANCE);
    }

}