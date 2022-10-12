package com.quesssystems.sistemato;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.beans.automacao.AutomacaoRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class AutomacaoRepositoryTests {
    @Autowired
    private AutomacaoRepository automacaoRepository;

    @Test
    public void testInsert() {
        Automacao automacao = new Automacao();
        automacao.setNome("Mensagens Whats");
        automacao.setDescricao("Envio de mensagens de texto e imagens pelo WhatsApp");
        automacao.setAtivo(true);

        Automacao automacaoSalva = automacaoRepository.save(automacao);

        Assertions.assertThat(automacaoSalva.getId()).isNotNull();
        Assertions.assertThat(automacaoSalva.getId()).isGreaterThan(0);
    }
}
