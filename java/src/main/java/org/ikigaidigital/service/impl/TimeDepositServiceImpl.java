package org.ikigaidigital.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
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
import java.util.Optional;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
@AllArgsConstructor
@Slf4j
public class TimeDepositServiceImpl implements TimeDepositService {

    private TimeDepositRepository timeDepositRepository;

    private TimeDepositWithWithdrawalsMapper timeDepositWithWithdrawalsMapper;

    private ObjectMapper objectMapper;

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

        Optional<TimeDepositEntity> timeDepositEntityOptional = Optional.empty();
        try {
            timeDepositEntityOptional = timeDepositRepository.findById(id);
        } catch (Exception exception) {
            log.error("Error while getting time deposit with id: " + id, exception);
            throw new TimeDepositNotFoundException("Time deposit with id: " + id + " not found");
        }

        if (timeDepositEntityOptional.isEmpty()) {
            log.error("Error while getting time deposit with id: " + id);
            throw new TimeDepositNotFoundException("Time deposit with id: " + id + " not found");
        }

        TimeDepositEntity timeDepositEntity = timeDepositEntityOptional.get();

        TimeDepositEntity updatedTimeDepositEntity = null;
        try {
            updatedTimeDepositEntity = applyPatchToTimeDepositEntity(updates, timeDepositEntity);
            updatedTimeDepositEntity = timeDepositRepository.saveAndFlush(updatedTimeDepositEntity);
        } catch (Exception exception) {
            log.error("Error while updating time deposit for time deposit with id: " + id);
            throw new InternalServerErrorException("Error while updating time deposit with id: " + id , exception);
        }

        if (isNull(updatedTimeDepositEntity)) {
            log.error("Error while updating time deposit for time deposit with id: " + id);
            throw new InternalServerErrorException("Error while updating time deposit with id: " + id);
        }

        return timeDepositWithWithdrawalsMapper.map(updatedTimeDepositEntity);
    }

    private TimeDepositEntity applyPatchToTimeDepositEntity(
            JsonPatch patch, TimeDepositEntity targetCustomer) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetCustomer, JsonNode.class));
        return objectMapper.treeToValue(patched, TimeDepositEntity.class);
    }

}
