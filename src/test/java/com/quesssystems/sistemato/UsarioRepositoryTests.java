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
    private static final Integer ID = 1;

    private static final String EMAIL = "pedroluiz.quessada@gmail.com";
    private static final String SENHA = "mrlouiz12";

    @Test
    public void testInsert() {
        Usuario usuario = new Usuario();
        usuario.setId(ID);
        usuario.setEmail(EMAIL);
        usuario.setSenha(SenhaUtil.criptografar(SENHA));
        usuario.setAdm(true);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        Assertions.assertThat(usuarioSalvo.getId()).isNotNull();
        Assertions.assertThat(usuarioSalvo.getId()).isGreaterThan(0);
    }
}
