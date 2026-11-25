package org.ikigaidigital.application.service;

import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.TimeDepositDetails;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.application.port.out.WithdrawalRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class GetAllTimeDepositsService implements GetAllTimeDepositsUseCase {

    private final TimeDepositRepository timeDepositRepository;
    private final WithdrawalRepository withdrawalRepository;

    public GetAllTimeDepositsService(TimeDepositRepository timeDepositRepository,
                                     WithdrawalRepository withdrawalRepository) {
        this.timeDepositRepository = timeDepositRepository;
        this.withdrawalRepository = withdrawalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeDepositDetails> getAllTimeDeposits() {
        Map<Integer, List<Withdrawal>> withdrawalsByDeposit = withdrawalRepository.findAll().stream()
                .collect(groupingBy(Withdrawal::timeDepositId));

        return timeDepositRepository.findAll().stream()
                .map(deposit -> new TimeDepositDetails(
                        deposit, withdrawalsByDeposit.getOrDefault(deposit.getId(), List.of())))
                .toList();
    }
}
