package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Coordinates deposit use cases while leaving SQL to the store and interest rules to the calculator. */
@Service
public class TimeDepositService {
    private final TimeDepositStore store;
    private final TimeDepositCalculator calculator;

    public TimeDepositService(TimeDepositStore store, TimeDepositCalculator calculator) {
        // Spring supplies these dependencies through the constructor when creating this service.
        this.store = store;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public List<TimeDepositView> getAllDeposits() {
        // Return the complete read model without recalculating or changing balances.
        return store.findAll();
    }

    @Transactional
    public void updateAllBalances() {
        // One transaction spans reading and saving, so a database failure cannot leave partial updates.
        List<TimeDeposit> deposits = store.findAllForCalculation();
        calculator.updateBalance(deposits);
        store.updateBalances(deposits);
    }
}
