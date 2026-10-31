package org.ikigaidigital.application;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Carries withdrawal data for application reads and API responses.
 * BigDecimal retains the stored decimal amount; LocalDate represents the SQL DATE without a time zone.
 * A record supplies the constructor and accessors id(), amount(), and date().
 */
public record WithdrawalView(int id, BigDecimal amount, LocalDate date) {
}
