package com.quesssystems.sistemato.beans.token;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<Token, Integer> {
    @Query("SELECT t FROM Token t WHERE t.codigo = ?1")
    Token findByCodigo(String codigo);

    @Query("SELECT MAX(t.id) FROM Token t")
    Integer findMaxId();
}
