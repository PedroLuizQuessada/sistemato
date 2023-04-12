package com.quesssystems.sistemato;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.beans.automacao.AutomacaoRepository;
import com.quesssystems.sistemato.beans.log.Log;
import com.quesssystems.sistemato.beans.log.LogRepository;
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

    @Test
    public void testInsert() {
        Optional<Automacao> optionalAutomacao = automacaoRepository.findById(1);
        Assertions.assertThat(optionalAutomacao.isPresent()).isEqualTo(true);
        Automacao automacao = optionalAutomacao.get();

        Log log = new Log();
        log.setAutomacao(automacao);
        log.setHora(new Timestamp(System.currentTimeMillis()));
        Log logSalva = logRepository.save(log);

        Assertions.assertThat(logSalva.getId()).isNotNull();
        Assertions.assertThat(logSalva.getId()).isGreaterThan(0);
    }
}
