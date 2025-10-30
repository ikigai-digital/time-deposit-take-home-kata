package org.ikigaidigital.util;

import org.ikigaidigital.entity.TimeDepositEntity;
import org.ikigaidigital.entity.WithdrawalEntity;
import org.ikigaidigital.model.PlanType;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.model.Withdrawal;
import org.ikigaidigital.web.domain.response.TimeDepositResponse;
import org.ikigaidigital.web.domain.response.WithdrawalResponse;

import java.time.OffsetDateTime;
import java.util.List;

public final class TestUtil {

    private TestUtil() {
    }

    private static final OffsetDateTime NOW = OffsetDateTime.now();

    public static final Withdrawal WITHDRAWAL_1 = new Withdrawal(10d, NOW);
    public static final Withdrawal WITHDRAWAL_2 = new Withdrawal(20d, OffsetDateTime.now());
    public static final Withdrawal WITHDRAWAL_3 = new Withdrawal(30d, OffsetDateTime.now());

    public static final WithdrawalResponse WITHDRAWAL_RESPONSE_1 = new WithdrawalResponse(WITHDRAWAL_1.getAmount(),
            WITHDRAWAL_1.getDate());
    public static final WithdrawalResponse WITHDRAWAL_RESPONSE_2 = new WithdrawalResponse(WITHDRAWAL_2.getAmount(),
            WITHDRAWAL_2.getDate());
    public static final WithdrawalResponse WITHDRAWAL_RESPONSE_3 = new WithdrawalResponse(WITHDRAWAL_3.getAmount(),
            WITHDRAWAL_3.getDate());

    public final static TimeDepositWithWithdrawals TIME_DEPOSIT_WITH_WITHDRAWALS_1 =
            new TimeDepositWithWithdrawals(1, PlanType.BASIC.name(), 100d, 365,
                    List.of(WITHDRAWAL_1));
    public final static TimeDepositWithWithdrawals TIME_DEPOSIT_WITH_WITHDRAWALS_2 =
            new TimeDepositWithWithdrawals(2, PlanType.STUDENT.name(), 200d, 900,
                    List.of(WITHDRAWAL_2, WITHDRAWAL_3));

    public final static TimeDepositResponse TIME_DEPOSIT_RESPONSE_1 =
            new TimeDepositResponse(TIME_DEPOSIT_WITH_WITHDRAWALS_1.getId(), TIME_DEPOSIT_WITH_WITHDRAWALS_1.getPlanType(),
                    TIME_DEPOSIT_WITH_WITHDRAWALS_1.getBalance(), TIME_DEPOSIT_WITH_WITHDRAWALS_1.getDays(),
                    List.of(WITHDRAWAL_RESPONSE_1));
    public final static TimeDepositResponse TIME_DEPOSIT_RESPONSE_2 =
            new TimeDepositResponse(TIME_DEPOSIT_WITH_WITHDRAWALS_2.getId(), TIME_DEPOSIT_WITH_WITHDRAWALS_2.getPlanType(),
                    TIME_DEPOSIT_WITH_WITHDRAWALS_2.getBalance(), TIME_DEPOSIT_WITH_WITHDRAWALS_2.getDays(),
                    List.of(WITHDRAWAL_RESPONSE_2, WITHDRAWAL_RESPONSE_3));

    public final static WithdrawalEntity WITHDRAWAL_ENTITY_1 = new WithdrawalEntity(1, 10d, NOW, null);
    public final static TimeDepositEntity TIME_DEPOSIT_ENTITY_1
            = new TimeDepositEntity(1, PlanType.BASIC.name(), 100d, 365, List.of(WITHDRAWAL_ENTITY_1));
    public final static TimeDepositEntity TIME_DEPOSIT_ENTITY_2
            = new TimeDepositEntity(2, PlanType.PREMIUM.name(), 200d, 770, List.of(WITHDRAWAL_ENTITY_1));

    static {
        WITHDRAWAL_ENTITY_1.setTimeDeposit(TIME_DEPOSIT_ENTITY_1);
    }
}
