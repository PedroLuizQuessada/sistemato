package com.quesssystems.sistemato.beans.automacao;

import com.quesssystems.sistemato.exceptions.AutomacaoNaoEncontradaException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AutomacaoService {
    private final AutomacaoRepository automacaoRepository;

    public AutomacaoService(AutomacaoRepository automacaoRepository) {
        this.automacaoRepository = automacaoRepository;
    }

    public List<Automacao> listAll(boolean ativo) {
        List<Automacao> automacoes = (List<Automacao>) automacaoRepository.findAll();
        automacoes.removeIf(automacao -> automacao.isAtivo() != ativo);
        return automacoes;
    }

    public Automacao get(Integer id) throws AutomacaoNaoEncontradaException {
        Optional<Automacao> automacaoOptional = automacaoRepository.findById(id);
        if (automacaoOptional.isPresent()) {
            return automacaoOptional.get();
        }
        else {
            throw new AutomacaoNaoEncontradaException(id);
        }
    }

    public boolean ativoToggle(Integer id) throws AutomacaoNaoEncontradaException {
        Automacao automacao = get(id);
        automacao.setAtivo(!automacao.isAtivo());
        automacaoRepository.save(automacao);
        return automacao.isAtivo();
    }

    public void delete(Integer id) throws AutomacaoNaoEncontradaException {
        get(id);
        automacaoRepository.deleteById(id);
    }

    public Automacao save(Automacao automacao) {
        if (automacao.getId() == null) {
            Integer idMax = automacaoRepository.findMaxId();
            if (idMax == null) {
                idMax = 1;
            } else {
                idMax = idMax + 1;
            }
            automacao.setId(idMax);
        }
        return automacaoRepository.save(automacao);
    }

    public boolean isHorarioInvalido(String horario) {
        int hora = Integer.parseInt(horario.substring(0, 2));
        int minuto = Integer.parseInt(horario.substring(3));

        return hora > 23 || minuto > 59;
    }

    public String recuperarTextoAutomacaoSalva(Automacao automacao) {
        String texto = "A automação foi salva com sucesso<br><br>";
        String textoDiasExecucao = "Dias configurados para execução: ";
        String textoHorarioExecucao = "Horário configurado para execução: ";

        if (automacao.isAtivo()) {
            texto = texto + "Automação rodando<br>";
        }
        else {
            texto = texto + "Automação parada<br>";
            textoDiasExecucao = "Dias configurados para execução quando a automação for ativada: ";
            textoHorarioExecucao = "Horário configurado para execução quando a automação for ativada: ";
        }

        if (!automacao.isDomingo() && !automacao.isSegunda() && !automacao.isTerca() && !automacao.isQuarta() &&
                !automacao.isQuinta() && !automacao.isSexta() && !automacao.isSabado()) {
            textoDiasExecucao = textoDiasExecucao + "nenhum";
        }
        else {
            if (automacao.isDomingo()) {
                textoDiasExecucao = textoDiasExecucao + "domingo, ";
            }
            if (automacao.isSegunda()) {
                textoDiasExecucao = textoDiasExecucao + "segunda, ";
            }
            if (automacao.isTerca()) {
                textoDiasExecucao = textoDiasExecucao + "terça, ";
            }
            if (automacao.isQuarta()) {
                textoDiasExecucao = textoDiasExecucao + "quarta, ";
            }
            if (automacao.isQuinta()) {
                textoDiasExecucao = textoDiasExecucao + "quinta, ";
            }
            if (automacao.isSexta()) {
                textoDiasExecucao = textoDiasExecucao + "sexta, ";
            }
            if (automacao.isSabado()) {
                textoDiasExecucao = textoDiasExecucao + "sábado, ";
            }
            textoDiasExecucao = textoDiasExecucao.substring(0, textoDiasExecucao.length() - 2);
        }
        texto = texto + textoDiasExecucao + "<br>";

        if (automacao.getHorarioInicio() == null || automacao.getHorarioInicio().length() == 0) {
            textoHorarioExecucao = textoHorarioExecucao + "das 00:00";
        }
        else {
            textoHorarioExecucao = textoHorarioExecucao + "das " + automacao.getHorarioInicio();
        }
        if (automacao.getHorarioFim() == null || automacao.getHorarioFim().length() == 0) {
            textoHorarioExecucao = textoHorarioExecucao + " até as 23:59";
        }
        else {
            textoHorarioExecucao = textoHorarioExecucao + " até as " + automacao.getHorarioInicio();
        }
        texto = texto + textoHorarioExecucao + "<br>";

        if (automacao.isHabilitarTexto()) {
            String textoTextoApoio = automacao.getOrientacoesTexto() + " " + automacao.getTexto();
            texto = texto + textoTextoApoio + "<br>";
        }

        return texto;
    }
}
