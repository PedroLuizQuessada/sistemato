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

    public void save(Automacao automacao) {
        if (automacao.getId() == null) {
            Integer idMax = automacaoRepository.findMaxId();
            if (idMax == null) {
                idMax = 1;
            } else {
                idMax = idMax + 1;
            }
            automacao.setId(idMax);
        }
        automacaoRepository.save(automacao);
    }

    public boolean isHorarioInvalido(String horario) {
        int hora = Integer.parseInt(horario.substring(0, 2));
        int minuto = Integer.parseInt(horario.substring(3));

        return hora > 23 || minuto > 59;
    }
}
