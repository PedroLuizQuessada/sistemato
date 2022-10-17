package com.quesssystems.sistemato;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.beans.automacao.AutomacaoRepository;
import com.quesssystems.sistemato.beans.execucao.Execucao;
import com.quesssystems.sistemato.beans.execucao.ExecucaoRepository;
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
public class ExecucaoRepositoryTests {
    @Autowired
    private ExecucaoRepository execucaoRepository;

    @Autowired
    private AutomacaoRepository automacaoRepository;

    @Test
    public void testInsert() {
        Optional<Automacao> optionalAutomacao = automacaoRepository.findById(1);
        Assertions.assertThat(optionalAutomacao.isPresent()).isEqualTo(true);
        Automacao automacao = optionalAutomacao.get();

        Execucao execucao = new Execucao();
        execucao.setAutomacao(automacao);
        execucao.setHoraExecucao(new Timestamp(System.currentTimeMillis()));
        Execucao execucaoSalva = execucaoRepository.save(execucao);

        Assertions.assertThat(execucaoSalva.getId()).isNotNull();
        Assertions.assertThat(execucaoSalva.getId()).isGreaterThan(0);
    }
}
