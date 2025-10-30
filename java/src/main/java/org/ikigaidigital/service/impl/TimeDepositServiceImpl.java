package org.ikigaidigital.service.impl;

import com.github.fge.jsonpatch.JsonPatch;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ikigaidigital.entity.TimeDepositEntity;
import org.ikigaidigital.mapper.TimeDepositWithWithdrawalsMapper;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.repository.TimeDepositRepository;
import org.ikigaidigital.service.TimeDepositService;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
@AllArgsConstructor
@Slf4j
public class TimeDepositServiceImpl implements TimeDepositService {

    private TimeDepositRepository timeDepositRepository;

    private TimeDepositWithWithdrawalsMapper timeDepositWithWithdrawalsMapper;

    @Override
    public List<TimeDepositWithWithdrawals> getTimeDeposits() {

        List<TimeDepositWithWithdrawals> result = List.of();

        try {
            List<TimeDepositEntity> timeDepositEntities = timeDepositRepository.findAll();

            if (isNotEmpty(timeDepositEntities)) {
                result = timeDepositEntities.stream().map(timeDepositWithWithdrawalsMapper::map).toList();
            }
        } catch (Exception exception) {
            log.error("Error while getting all time deposits");
            throw exception;
        }

        return result;
    }

    @Override
    public TimeDepositWithWithdrawals update(int id, JsonPatch updates) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
