package com.quesssystems.sistemato;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.beans.automacao.AutomacaoRepository;
import com.quesssystems.sistemato.beans.pendencia.Pendencia;
import com.quesssystems.sistemato.beans.pendencia.PendenciaRepository;
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
public class PendenciaRepositoryTests {
    @Autowired
    private PendenciaRepository pendenciaRepository;
    @Autowired
    private AutomacaoRepository automacaoRepository;
    private static final int ID_AUTOMACAO = 1;
    private static final int NUM_PENDENCIAS = 8;
    private static final String NOME_ARQUIVO = "teste.xlsx";
    private static final String JSON = "{\"numero\":\"1999698-3788\",\"data\":\"25/12/2023\"}";
    @Test
    public void testInsert() {
        Optional<Automacao> optionalAutomacao = automacaoRepository.findById(ID_AUTOMACAO);
        Assertions.assertThat(optionalAutomacao.isPresent()).isEqualTo(true);
        Automacao automacao = optionalAutomacao.get();

        for (int i = 0; i < NUM_PENDENCIAS; i++) {
            Pendencia pendencia = new Pendencia();
            pendencia.setAutomacao(automacao);
            pendencia.setNomeArquivo(NOME_ARQUIVO);
            pendencia.setDataHoraUpload(new Timestamp(System.currentTimeMillis()));
            pendencia.setProcessado(false);
            pendencia.setDataHoraProcessamento(null);
            pendencia.setJson(JSON);
            Pendencia pendenciaSalva = pendenciaRepository.save(pendencia);

            Assertions.assertThat(pendenciaSalva.getId()).isNotNull();
            Assertions.assertThat(pendenciaSalva.getId()).isGreaterThan(0);
        }
    }
}
