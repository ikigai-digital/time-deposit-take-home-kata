package org.ikigaidigital.timedeposit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.ikigaidigital.timedeposit.timedeposit.domain.TimeDeposit;
import org.ikigaidigital.timedeposit.timedeposit.domain.TimeDepositCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


class TimeDepositCalculatorTest {

	private static final String BASIC = "basic";
	private static final double BALANCE = 1000.00;

	private final TimeDepositCalculator calculator = new TimeDepositCalculator();

	@ParameterizedTest(name = "{0}, {1} days: {2} -> {3}")
	@CsvSource({
			// no interest for the first 30 days, any plan
			"basic,    0,    1000.00, 1000.00",
			"basic,    30,   1000.00, 1000.00",
			"student,  30,   1000.00, 1000.00",
			"premium,  30,   1000.00, 1000.00",
			// basic: 1% p.a. -> 1000 * 0.01 / 12 = 0.8333 -> 0.83
			"basic,    31,   1000.00, 1000.83",
			"basic,    45,   1000.00, 1000.83",
			"basic,    366,  1000.00, 1000.83",
			"basic,    1000, 1000.00, 1000.83",
			// student: 3% p.a. -> 2.50, only while days < 366
			"student,  31,   1000.00, 1002.50",
			"student,  365,  1000.00, 1002.50",
			"student,  366,  1000.00, 1000.00",
			"student,  1000, 1000.00, 1000.00",
			// premium: 5% p.a. -> 4.1667 -> 4.17, only when days > 45
			"premium,  31,   1000.00, 1000.00",
			"premium,  45,   1000.00, 1000.00",
			"premium,  46,   1000.00, 1004.17",
			"premium,  1000, 1000.00, 1004.17",
			// interest rounded HALF_UP to 2 dp: 1234567 * 0.01 / 12 = 1028.8058 -> 1028.81
			"basic,    45,   1234567.00, 1235595.81",
			// HALF_UP is applied to the exact binary value of the double (new BigDecimal(double)):
			"premium,  46,   30.00,   30.13",     // 0.125 exactly -> rounds up to 0.13
			"basic,    31,   18.00,   18.01",     // "0.015" is 0.01499999... in binary -> rounds down to 0.01
			"basic,    31,   6.00,    6.01",      // "0.005" is 0.00500000...01 in binary -> rounds up to 0.01
			"basic,    31,   0.60,    0.60",      // interest below half a cent -> 0.00
			// zero and negative balances are not validated
			"basic,    31,   0.00,    0.00",
			"basic,    31,   -1000.00, -1000.83", // HALF_UP rounds away from zero: -0.8333 -> -0.83
			// unknown, empty or differently-cased plan types earn nothing
			"gold,     100,  1000.00, 1000.00",
			"BASIC,    100,  1000.00, 1000.00",
			"'',       100,  1000.00, 1000.00",
	})
	void updateBalance_appliesMonthlyInterestPerPlan(String planType, int days, double balance, double expected) {
		TimeDeposit deposit = new TimeDeposit(1, planType, balance, days);

		calculator.updateBalance(List.of(deposit));

		assertThat(deposit.getBalance()).isEqualTo(expected);
	}

	@Test
	void updateBalance_updatesEveryDepositInTheList() {
		List<TimeDeposit> deposits = List.of(
				new TimeDeposit(1, BASIC, BALANCE, 31),
				new TimeDeposit(2, "student", BALANCE, 31),
				new TimeDeposit(3, "premium", BALANCE, 46),
				new TimeDeposit(4, "premium", BALANCE, 10));

		calculator.updateBalance(deposits);

		assertThat(deposits).extracting(TimeDeposit::getBalance).containsExactly(1000.83, 1002.50, 1004.17, 1000.00);
	}

	@Test
	void updateBalance_doesNotChangeIdPlanTypeOrDays() {
		TimeDeposit deposit = new TimeDeposit(7, BASIC, BALANCE, 31);

		calculator.updateBalance(List.of(deposit));

		assertThat(deposit.getId()).isEqualTo(7);
		assertThat(deposit.getPlanType()).isEqualTo(BASIC);
		assertThat(deposit.getDays()).isEqualTo(31);
	}

	@Test
	void updateBalance_compoundsWhenCalledRepeatedly() {
		TimeDeposit deposit = new TimeDeposit(1, BASIC, BALANCE, 31);

		calculator.updateBalance(List.of(deposit));
		calculator.updateBalance(List.of(deposit));

		// second run adds 0.83, but the balance is a double: 1000.83 + 0.83 = 1001.6600000000001 (quirk pinned on
		// purpose; the persistence layer stores balances as DECIMAL(19,2), so the API never exposes it)
		assertThat(deposit.getBalance()).isEqualTo(1001.6600000000001);
	}

	@Test
	void updateBalance_emptyListIsANoOp() {
		assertThatNoException().isThrownBy(() -> calculator.updateBalance(new ArrayList<>()));
	}

	@Test
	void updateBalance_nullPlanTypeAfter30DaysThrows() {
		List<TimeDeposit> deposits = List.of(new TimeDeposit(1, null, BALANCE, 31));

		assertThatThrownBy(() -> calculator.updateBalance(deposits)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void updateBalance_nullBalanceThrowsForAnyPlanAndDays() {
		List<TimeDeposit> deposits = List.of(new TimeDeposit(1, BASIC, null, 10));

		assertThatThrownBy(() -> calculator.updateBalance(deposits)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void updateBalance_nullListThrows() {
		assertThatThrownBy(() -> calculator.updateBalance(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void updateBalance_nullPlanTypeWithin30DaysIsIgnored() {
		TimeDeposit deposit = new TimeDeposit(1, null, BALANCE, 30);

		calculator.updateBalance(List.of(deposit));

		assertThat(deposit.getBalance()).isEqualTo(BALANCE);
	}
}
