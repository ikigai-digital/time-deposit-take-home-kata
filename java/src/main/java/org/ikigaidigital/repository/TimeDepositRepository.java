package org.ikigaidigital.repository;

import java.util.List;
import org.ikigaidigital.entity.TimeDeposit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeDepositRepository extends CrudRepository<TimeDeposit, Integer> {

    List<TimeDeposit> findAll();
}
