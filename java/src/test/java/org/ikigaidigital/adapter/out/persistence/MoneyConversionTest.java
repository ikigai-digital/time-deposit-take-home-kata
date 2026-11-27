package org.ikigaidigital.adapter.out.persistence;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyConversionTest {

    @ParameterizedTest
    @CsvSource({
            "10008.33,   10008.33",
            "0.1,        0.10",
            "10000.005,  10000.01",
            "10000.004,  10000.00",
            "20083.3333, 20083.33",
    })
    void roundsToCentsHalfUp(double amount, String expected) {
        assertThat(TimeDepositPersistenceAdapter.toMoney(amount)).isEqualByComparingTo(expected)
                .hasScaleOf(2);
    }

    @ParameterizedTest
    @CsvSource({"0.1, 0.2"})
    void absorbsFloatingPointNoise(double a, double b) {
        // 0.1 + 0.2 == 0.30000000000000004 as a double
        assertThat(TimeDepositPersistenceAdapter.toMoney(a + b)).isEqualByComparingTo("0.30");
    }
}
