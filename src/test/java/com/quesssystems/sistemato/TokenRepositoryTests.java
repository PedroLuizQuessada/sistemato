package com.quesssystems.sistemato;

import com.quesssystems.sistemato.beans.token.Token;
import com.quesssystems.sistemato.beans.token.TokenRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class TokenRepositoryTests {
    @Autowired
    private TokenRepository tokenRepository;
    private static final Integer ID = 1;
    private static final String NOME = "Máquina 1";
    private static final boolean ATIVO = true;
    private static final String CODIGO = "0d2d81d9-9bfe-47e2-9aec-37b56795e4b5";
    @Test
    public void testInsert() {
        Token token = new Token();
        token.setId(ID);
        token.setNome(NOME);
        token.setAtivo(ATIVO);
        token.setCodigo(CODIGO);

        Token tokenSalvo = tokenRepository.save(token);

        Assertions.assertThat(tokenSalvo.getId()).isNotNull();
        Assertions.assertThat(tokenSalvo.getId()).isGreaterThan(0);
    }
}
