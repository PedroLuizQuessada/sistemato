package com.quesssystems.sistemato.beans.usuario;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository repo;

    public CustomUsuarioDetailsService(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = repo.findByEmail(email);
        if (usuario == null) {
            throw new UsernameNotFoundException(String.format("E-mail %s não está cadastrado", email));
        }

        return new CustomUsuarioDetails(usuario);
    }
}
