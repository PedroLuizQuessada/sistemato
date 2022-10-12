package com.quesssystems.sistemato;

import com.quesssystems.sistemato.beans.usuario.Usuario;
import com.quesssystems.sistemato.beans.usuario.UsuarioRepository;
import com.quesssystems.sistemato.util.SenhaUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class UsarioRepositoryTests {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void testInsert() {
        String senha = "mrlouiz12";

        Usuario usuario = new Usuario();
        usuario.setEmail("pedroluiz.quessada@gmail.com");
        usuario.setSenha(SenhaUtil.criptografar(senha));
        usuario.setTentativasAcesso(0);
        usuario.setAdm(true);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        Assertions.assertThat(usuarioSalvo.getId()).isNotNull();
        Assertions.assertThat(usuarioSalvo.getId()).isGreaterThan(0);
    }
}
