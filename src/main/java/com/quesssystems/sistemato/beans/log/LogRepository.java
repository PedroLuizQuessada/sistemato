package com.quesssystems.sistemato.beans.log;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LogRepository extends CrudRepository<Log, Integer> {
    @Query(value="SELECT * FROM Log l WHERE l.id_automacao = ?1 ORDER BY l.id DESC LIMIT ?2", nativeQuery = true)
    List<Log> listUltimosRegistros(Integer automacao, Integer numRegistros);
}
