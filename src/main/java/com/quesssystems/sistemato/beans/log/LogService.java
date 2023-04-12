package com.quesssystems.sistemato.beans.log;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {
    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<Log> listAll(Automacao automacao) {
        List<Log> logs = (List<Log>) logRepository.findAll();
        logs.removeIf(log -> log.getAutomacao() != automacao);
        return logs;
    }

    public void deleteByAutomacao(Automacao automacao) {
        List<Log> logs = listAll(automacao);
        logRepository.deleteAll(logs);
    }
}
