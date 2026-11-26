package org.ikigaidigital.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stored time deposit and its withdrawals.
 * The calculator keeps using {@link org.ikigaidigital.TimeDeposit}; that class
 * is unchanged because its shape is shared with the interest calculation.
 */
public record TimeDeposit(int id, String planType, BigDecimal balance, int days, List<Withdrawal> withdrawals) {
}
