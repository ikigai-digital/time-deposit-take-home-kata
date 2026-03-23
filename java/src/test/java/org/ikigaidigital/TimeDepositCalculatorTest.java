package org.ikigaidigital;

import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TimeDepositCalculatorTest {
    @ParameterizedTest
    @CsvSource({
        "basic, 1234.56, 30, 1234.56",    // No interest <= 30 days
        "basic, 1234.56, 31, 1235.59",    // 1% interest: 1234.56 * 0.01 / 12 = 1.03
        "student, 1234.56, 30, 1234.56",  // No interest <= 30 days
        "student, 1234.56, 31, 1237.65",  // 3% interest: 1234.56 * 0.03 / 12 = 3.09
        "student, 1234.56, 366, 1234.56", // No interest >= 366 days
        "premium, 1234.56, 45, 1234.56",  // No interest <= 45 days
        "premium, 1234.56, 46, 1239.70",  // 5% interest: 1234.56 * 0.05 / 12 = 5.14
        "unknown, 1234.56, 33, 1234.56"   // No interest due to an unknown plan type
    })
    public void updateBalance_Test(String planType, double balance, int days, double expectedBalance) {
        TimeDepositCalculator calc = new TimeDepositCalculator();
        TimeDeposit deposit = new TimeDeposit(1, planType, balance, days);

        calc.updateBalance(Collections.singletonList(deposit));

        assertThat(deposit.getBalance()).isCloseTo(expectedBalance, within(0.001));
    }
}
