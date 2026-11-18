package org.ikigaidigital.timedeposit.infrastructure.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.ikigaidigital.timedeposit.domain.TimeDeposit;
import org.ikigaidigital.timedeposit.infrastructure.entity.TimeDepositEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeDepositMapper {

	public static TimeDeposit toDomain(TimeDepositEntity entity) {
		return new TimeDeposit(entity.getId(), entity.getPlanType(), entity.getBalance().doubleValue(), entity.getDays());
	}

}
