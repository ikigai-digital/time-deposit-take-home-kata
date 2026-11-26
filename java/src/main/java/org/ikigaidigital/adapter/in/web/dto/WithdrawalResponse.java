package org.ikigaidigital.adapter.in.web.dto;

import java.time.LocalDate;

public record WithdrawalResponse(int id, int timeDepositId, String amount, LocalDate date) {
}
