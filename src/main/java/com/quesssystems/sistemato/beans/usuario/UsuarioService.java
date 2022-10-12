package com.quesssystems.sistemato.beans.usuario;

import com.quesssystems.sistemato.exceptions.UsuarioNaoEncontradoException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listAll() {
        return (List<Usuario>) usuarioRepository.findAll();
    }

    public Usuario get(Integer id) throws UsuarioNaoEncontradoException {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if (usuarioOptional.isPresent()) {
            return usuarioOptional.get();
        }
        else {
            throw new UsuarioNaoEncontradoException(id);
        }
    }

    public boolean blockToggle(Integer id) throws UsuarioNaoEncontradoException {
        Usuario usuario = get(id);
        if (usuario.getTentativasAcesso() == 3) {
            usuario.setTentativasAcesso(0);
        }
        else {
            usuario.setTentativasAcesso(3);
        }
        usuarioRepository.save(usuario);
        return usuario.getTentativasAcesso() == 3;
    }

    public boolean admToggle(Integer id) throws UsuarioNaoEncontradoException {
        Usuario usuario = get(id);
        usuario.setAdm(!usuario.isAdm());
        usuarioRepository.save(usuario);
        return usuario.isAdm();
    }

    public void delete(Integer id) throws UsuarioNaoEncontradoException {
        get(id);
        usuarioRepository.deleteById(id);
    }

    public void save(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    public Usuario getUsuarioLogado() throws UsuarioNaoEncontradoException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String emailUsuarioLogado = authentication.getName();
            return usuarioRepository.findByEmail(emailUsuarioLogado);
        }

        throw new UsuarioNaoEncontradoException();
    }
}
