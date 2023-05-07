package com.quesssystems.sistemato.beans.usuario;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UsuarioRepository extends CrudRepository<Usuario, Integer> {
    @Query("SELECT u FROM Usuario u WHERE u.email = ?1")
    Usuario findByEmail(String email);

    @Query("SELECT MAX(u.id) FROM Usuario u")
    Integer findMaxId();
}
