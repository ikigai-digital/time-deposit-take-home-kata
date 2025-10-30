package org.ikigaidigital.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ikigaidigital.entity.TimeDepositEntity;
import org.ikigaidigital.exception.InternalServerErrorException;
import org.ikigaidigital.exception.TimeDepositNotFoundException;
import org.ikigaidigital.mapper.TimeDepositWithWithdrawalsMapper;
import org.ikigaidigital.model.TimeDepositWithWithdrawals;
import org.ikigaidigital.repository.TimeDepositRepository;
import org.ikigaidigital.service.TimeDepositService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.ikigaidigital.util.PropertiesUtil.updateProperty;

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
    public TimeDepositWithWithdrawals update(int id, final Map<String, Object> updates) {

        TimeDepositEntity timeDepositEntity;
        try {
            timeDepositEntity = timeDepositRepository.findById(id)
                    .orElseThrow(() -> new TimeDepositNotFoundException("Time deposit with id: " + id + " not found"));
        } catch (Exception exception) {
            log.error("Time deposit with id: " + id + " not found", exception);
            throw new TimeDepositNotFoundException("Time deposit with id: " + id + " not found");
        }

        TimeDepositEntity updatedTimeDepositEntity;
        try {
            updates.forEach((propertyToUpdate, valueToUpdate) ->
                    updateProperty(timeDepositEntity, propertyToUpdate, valueToUpdate));

            updatedTimeDepositEntity = timeDepositRepository.saveAndFlush(timeDepositEntity);
        } catch (Exception exception) {
            log.error("Error while updating time deposit for time deposit with id: " + id, exception);
            throw new InternalServerErrorException("Error while updating time deposit with id: " + id, exception);
        }

        if (isNull(updatedTimeDepositEntity)) {
            log.error("Error while updating time deposit for time deposit with id: " + id);
            throw new InternalServerErrorException("Error while updating time deposit with id: " + id);
        }

        return timeDepositWithWithdrawalsMapper.map(updatedTimeDepositEntity);
    }

}
