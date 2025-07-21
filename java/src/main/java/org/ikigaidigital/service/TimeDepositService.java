package org.ikigaidigital.service;

import java.util.List;
import org.ikigaidigital.entity.TimeDeposit;
import org.ikigaidigital.mapper.TimeDepositMapper;
import org.ikigaidigital.model.TimeDepositResponse;
import org.ikigaidigital.repository.TimeDepositRepository;
import org.springframework.stereotype.Service;

@Service
public class TimeDepositService {

    private final TimeDepositRepository timeDepositRepository;
    private final TimeDepositMapper timeDepositMapper;
    private final TimeDepositCalculator timeDepositCalculator;

    public TimeDepositService(TimeDepositMapper timeDepositMapper, TimeDepositRepository timeDepositRepository, TimeDepositCalculator timeDepositCalculator) {
        this.timeDepositMapper = timeDepositMapper;
        this.timeDepositRepository = timeDepositRepository;
        this.timeDepositCalculator = timeDepositCalculator;
    }

    public List<TimeDepositResponse> getAllTimeDeposits() {
        return timeDepositMapper.fromEntityToApiResponse(timeDepositRepository.findAll());
    }

    public void updateBalances() {
        List<TimeDeposit> timeDepositList = timeDepositRepository.findAll();
        if (timeDepositList.isEmpty()) {
            return;
        }

        List<org.ikigaidigital.model.TimeDeposit> timeDepositResponseList =
            timeDepositMapper.fromEntityToModel(timeDepositList);

        timeDepositCalculator.updateBalance(timeDepositResponseList);
        timeDepositMapper.fromModelToEntity(timeDepositResponseList);
        timeDepositRepository.saveAll(timeDepositList);
    }
}

