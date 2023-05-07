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
    private static final Integer ID = 1;
    private static final String NOME = "Mensagens Whats";
    private static final String DESCRICAO = "Envio de mensagens de texto e imagens pelo WhatsApp";
    private static final boolean ATIVO = true;
    private static final boolean DOMINGO = false;
    private static final boolean SEGUNDA = true;
    private static final boolean TERCA = true;
    private static final boolean QUARTA = true;
    private static final boolean QUINTA = true;
    private static final boolean SEXTA = true;
    private static final boolean SABADO = false;
    private static final String HORARIO_INICIO = "08:00";
    private static final String HORARIO_FIM = "18:00";
    private static final String ORIENTACOES_PENDENCIAS = "Coluna 1: número de WhatsApp; Coluna 2: data de retorno";
    private static final String ESTRUTURA = "numero;data";
    private static final boolean HABILITAR_TEXTO = true;
    private static final String ORIENTACOES_TEXTO = "Mensagens a serem enviadas separadas por ';':";
    private static final String TEXTO = "Mensagem 1;Mensagem 2";

    @Test
    public void testInsert() {
        Automacao automacao = new Automacao();
        automacao.setId(ID);
        automacao.setNome(NOME);
        automacao.setDescricao(DESCRICAO);
        automacao.setAtivo(ATIVO);
        automacao.setDomingo(DOMINGO);
        automacao.setSegunda(SEGUNDA);
        automacao.setTerca(TERCA);
        automacao.setQuarta(QUARTA);
        automacao.setQuinta(QUINTA);
        automacao.setSexta(SEXTA);
        automacao.setSabado(SABADO);
        automacao.setHorarioInicio(HORARIO_INICIO);
        automacao.setHorarioFim(HORARIO_FIM);
        automacao.setOrientacoesPendencias(ORIENTACOES_PENDENCIAS);
        automacao.setEstrutura(ESTRUTURA);
        automacao.setHabilitarTexto(HABILITAR_TEXTO);
        automacao.setOrientacoesTexto(ORIENTACOES_TEXTO);
        automacao.setTexto(TEXTO);

        Automacao automacaoSalva = automacaoRepository.save(automacao);

        Assertions.assertThat(automacaoSalva.getId()).isNotNull();
        Assertions.assertThat(automacaoSalva.getId()).isGreaterThan(0);
    }
}
