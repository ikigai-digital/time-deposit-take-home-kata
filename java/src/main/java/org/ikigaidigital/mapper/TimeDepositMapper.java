package org.ikigaidigital.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.ikigaidigital.entity.PlanType;
import org.ikigaidigital.entity.TimeDeposit;
import org.ikigaidigital.model.TimeDepositResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeDepositMapper {

    private final WithdrawalMapper withdrawalMapper;

    @Autowired
    public TimeDepositMapper(WithdrawalMapper withdrawalMapper) {
        this.withdrawalMapper = withdrawalMapper;
    }

    public List<TimeDepositResponse> fromEntityToApiResponse(List<TimeDeposit> timeDeposits) {
        if (timeDeposits == null) {
            return List.of();
        }
        return timeDeposits.stream()
            .map(this::toTimeDepositResponse)
            .toList();
    }

    public List<org.ikigaidigital.model.TimeDeposit> fromEntityToModel(List<TimeDeposit> timeDeposits) {
        if (timeDeposits == null) {
            return List.of();
        }
        return timeDeposits.stream()
            .map(this::toModelTimeDeposit)
            .toList();
    }

    public List<TimeDeposit> fromModelToEntity(List<org.ikigaidigital.model.TimeDeposit> timeDeposits) {
        if (timeDeposits == null) {
            return List.of();
        }
        return timeDeposits.stream()
            .map(this::toEntityTimeDeposit)
            .toList();
    }

    private TimeDeposit toEntityTimeDeposit(org.ikigaidigital.model.TimeDeposit timeDeposit) {
        return TimeDeposit.builder()
            .planType(PlanType.valueOf(timeDeposit.getPlanType()))
            .days(timeDeposit.getDays())
            .balance(BigDecimal.valueOf(timeDeposit.getBalance()))
            .build();
    }

    private org.ikigaidigital.model.TimeDeposit toModelTimeDeposit(TimeDeposit timeDeposit) {
        return new org.ikigaidigital.model.TimeDeposit(
            timeDeposit.getId(),
            timeDeposit.getPlanType().toString(),
            timeDeposit.getBalance().doubleValue(),
            timeDeposit.getDays()
        );
    }

    private TimeDepositResponse toTimeDepositResponse(TimeDeposit timeDeposit) {
        return new TimeDepositResponse(
            String.valueOf(timeDeposit.getId()),
            timeDeposit.getPlanType().toString(),
            timeDeposit.getBalance(),
            timeDeposit.getDays(),
            withdrawalMapper.toWithdrawalResponse(timeDeposit.getWithdrawals())
        );
    }

}
