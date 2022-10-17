package com.quesssystems.sistemato.beans.execucao;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecucaoService {
    private final ExecucaoRepository execucaoRepository;

    public ExecucaoService(ExecucaoRepository execucaoRepository) {
        this.execucaoRepository = execucaoRepository;
    }

    public List<Execucao> listAll(Automacao automacao) {
        List<Execucao> execucoes = (List<Execucao>) execucaoRepository.findAll();
        execucoes.removeIf(execucao -> execucao.getAutomacao() != automacao);
        return execucoes;
    }
}
