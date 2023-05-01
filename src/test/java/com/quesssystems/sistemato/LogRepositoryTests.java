package com.quesssystems.sistemato;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.beans.automacao.AutomacaoRepository;
import com.quesssystems.sistemato.beans.log.Log;
import com.quesssystems.sistemato.beans.log.LogRepository;
import com.quesssystems.sistemato.beans.token.Token;
import com.quesssystems.sistemato.beans.token.TokenRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.sql.Timestamp;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class LogRepositoryTests {
    @Autowired
    private LogRepository logRepository;

    @Autowired
    private AutomacaoRepository automacaoRepository;

    @Autowired
    private TokenRepository tokenRepository;
    private static final int ID_AUTOMACAO = 1;
    private static final String TOKEN = "0d2d81d9-9bfe-47e2-9aec-37b56795e4b5";

    private static final int NUM_LOGS = 8;

    @Test
    public void testInsert() {
        Optional<Automacao> optionalAutomacao = automacaoRepository.findById(ID_AUTOMACAO);
        Assertions.assertThat(optionalAutomacao.isPresent()).isEqualTo(true);
        Automacao automacao = optionalAutomacao.get();

        Token token = tokenRepository.findByCodigo(TOKEN);
        Assertions.assertThat(token).isNotNull();

        for (int i = 0; i < NUM_LOGS; i++) {
            Log log = new Log();
            log.setAutomacao(automacao);
            log.setToken(token);
            log.setHora(new Timestamp(System.currentTimeMillis()));
            log.setMensagem(String.format("teste %d", i + 1));
            Log logSalva = logRepository.save(log);

            Assertions.assertThat(logSalva.getId()).isNotNull();
            Assertions.assertThat(logSalva.getId()).isGreaterThan(0);
        }
    }
}
