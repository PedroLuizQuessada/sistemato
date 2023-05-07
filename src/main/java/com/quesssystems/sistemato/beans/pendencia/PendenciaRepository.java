package com.quesssystems.sistemato.beans.pendencia;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PendenciaRepository extends CrudRepository<Pendencia, Integer> {
    @Query("SELECT p FROM Pendencia p WHERE p.automacao.id = ?1")
    List<Pendencia> findByAutomacao(Integer idAutomacao);

    @Query("SELECT MAX(p.id) FROM Pendencia p")
    Integer findMaxId();
}
