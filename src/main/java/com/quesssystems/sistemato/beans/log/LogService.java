package com.quesssystems.sistemato.beans.log;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.beans.token.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class LogService {
    @Value("${sistemato.num-logs}")
    private Integer numRegistrosLog;
    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<Log> listAllByAutomacao(Automacao automacao) {
        List<Log> logs = (List<Log>) logRepository.findAll();
        logs.removeIf(log -> log.getAutomacao() != automacao);
        return logs;
    }

    public List<Log> listAllByToken(Token token) {
        List<Log> logs = (List<Log>) logRepository.findAll();
        logs.removeIf(log -> log.getToken() != token);
        return logs;
    }

    public List<Log> listUltimosRegistros(List<Log> logs) {
        try {
            Collections.reverse(logs);
            return logs.subList(0, numRegistrosLog);
        }
        catch (IndexOutOfBoundsException e) {
            return logs;
        }
    }

    public void save(Log log) {
        if (log.getId() == null) {
            Integer idMax = logRepository.findMaxId();
            if (idMax == null) {
                idMax = 1;
            } else {
                idMax = idMax + 1;
            }
            log.setId(idMax);
        }
        logRepository.save(log);
    }

    public void deleteByAutomacao(Automacao automacao) {
        List<Log> logs = listAllByAutomacao(automacao);
        logRepository.deleteAll(logs);
    }

    public void updateByTokenDelete(Token token) {
        for (Log log : listAllByToken(token)) {
            log.setToken(null);
            logRepository.save(log);
        }
    }
}
