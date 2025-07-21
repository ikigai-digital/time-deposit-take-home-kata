package org.ikigaidigital.mapper;

import java.util.List;
import org.ikigaidigital.entity.Withdrawal;
import org.ikigaidigital.model.WithdrawalResponse;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalMapper {

    public List<WithdrawalResponse> toWithdrawalResponse(List<Withdrawal> withdrawalEntity) {
        if (withdrawalEntity == null) {
            return List.of();
        }
        return withdrawalEntity.stream()
            .map(withdrawal -> new WithdrawalResponse(
                String.valueOf(withdrawal.getId()),
                withdrawal.getAmount(),
                withdrawal.getDate().toString()))
            .toList();
    }
}
