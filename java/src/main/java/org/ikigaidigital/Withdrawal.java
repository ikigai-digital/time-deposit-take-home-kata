package org.ikigaidigital;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A withdrawal made from a time deposit.
 * <p>
 * Kept separate from {@link TimeDeposit}, which must not change, and linked to it by {@code timeDepositId}.
 * New type, so money uses {@link BigDecimal} (unlike the legacy {@code Double} balance).
 * Assumption: withdrawals are informational only; the README does not say they affect interest.
 */
public record Withdrawal(int id, int timeDepositId, BigDecimal amount, LocalDate date) {
}
