package com.quesssystems.sistemato.beans.log;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface LogRepository extends CrudRepository<Log, Integer> {
    @Query("SELECT MAX(l.id) FROM Log l")
    Integer findMaxId();
}
