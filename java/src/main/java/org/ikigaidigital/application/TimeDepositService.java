package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.TimeDepositCalculator;
import org.ikigaidigital.application.port.in.GetAllTimeDepositsUseCase;
import org.ikigaidigital.application.port.in.UpdateAllBalancesUseCase;
import org.ikigaidigital.application.port.out.TimeDepositRepository;
import org.ikigaidigital.application.port.out.WithdrawalRepository;
import org.ikigaidigital.domain.Withdrawal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TimeDepositService implements GetAllTimeDepositsUseCase, UpdateAllBalancesUseCase {

    private final TimeDepositRepository timeDepositRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final TimeDepositCalculator timeDepositCalculator;

    public TimeDepositService(TimeDepositRepository timeDepositRepository,
                              WithdrawalRepository withdrawalRepository,
                              TimeDepositCalculator timeDepositCalculator) {
        this.timeDepositRepository = timeDepositRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.timeDepositCalculator = timeDepositCalculator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeDepositWithWithdrawals> getAllTimeDeposits() {
        Map<Integer, List<Withdrawal>> withdrawalsByTimeDepositId = withdrawalRepository.findAll().stream()
                .collect(Collectors.groupingBy(Withdrawal::timeDepositId));
        return timeDepositRepository.findAll().stream()
                .map(timeDeposit -> new TimeDepositWithWithdrawals(
                        timeDeposit,
                        withdrawalsByTimeDepositId.getOrDefault(timeDeposit.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public void updateAllBalances() {
        List<TimeDeposit> timeDeposits = timeDepositRepository.findAll();
        timeDepositCalculator.updateBalance(timeDeposits);
        timeDepositRepository.updateBalances(timeDeposits);
    }
}
