package org.ikigaidigital.application;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carries the five required deposit fields for application reads and API responses.
 * BigDecimal retains the stored balance; withdrawals belong to this deposit and are empty when absent.
 * The existing TimeDeposit remains the separate, unchanged model used by the calculator.
 */
public record TimeDepositView(int id, String planType, BigDecimal balance, int days,
                              List<WithdrawalView> withdrawals) {
}
