package com.quesssystems.sistemato.beans.automacao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AutomacaoRepository extends CrudRepository<Automacao, Integer> {
    @Query("SELECT MAX(a.id) FROM Automacao a")
    Integer findMaxId();
}
