package com.quesssystems.sistemato.beans.pendencia;

import com.google.gson.Gson;
import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.exceptions.PendenciaNaoEncontradaException;
import com.quesssystems.sistemato.util.DatabaseUtil;
import com.quesssystems.sistemato.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class PendenciaService {
    @Value("${sistemato.num-pendencias}")
    private Integer numRegistrosPendencia;
    private final Gson gson = new Gson();
    private final FileUtil fileUtil;
    private final DatabaseUtil databaseUtil;
    private final PendenciaRepository pendenciaRepository;

    public PendenciaService(FileUtil fileUtil, DatabaseUtil databaseUtil, PendenciaRepository pendenciaRepository) {
        this.fileUtil = fileUtil;
        this.databaseUtil = databaseUtil;
        this.pendenciaRepository = pendenciaRepository;
    }

    public List<Pendencia> converterPendencia(Automacao automacao, String nomeArquivo, List<List<List<String>>> planilhas) {
        List<Pendencia> pendencias = new ArrayList<>();
        for (List<List<String>> planilha : planilhas) {
            for (List<String> linha : planilha) {
                Pendencia pendencia = new Pendencia();
                pendencia.setAutomacao(automacao);
                pendencia.setNomeArquivo(nomeArquivo);
                pendencia.setDataHoraUpload(databaseUtil.recuperarHoraAtualComFuso());
                Map<String, String> mapa = new HashMap<>();
                List<String> colunas = Arrays.asList(automacao.getEstrutura().split(";"));
                for (String coluna : colunas) {
                    mapa.put(coluna, linha.get(colunas.indexOf(coluna)));
                }
                pendencia.setJson(gson.toJson(mapa));
                pendencias.add(pendencia);
            }
        }

        return pendencias;
    }

    public void save(List<Pendencia> pendencias) {
        pendenciaRepository.saveAll(pendencias);
    }

    public List<Pendencia> listAll(Automacao automacao) {
        List<Pendencia> pendencias = (List<Pendencia>) pendenciaRepository.findAll();
        pendencias.removeIf(pendencia -> pendencia.getAutomacao() != automacao);
        return pendencias;
    }

    public List<Pendencia> listUltimosRegistros(List<Pendencia> pendencias) {
        try {
            Collections.reverse(pendencias);
            return pendencias.subList(0, numRegistrosPendencia);
        }
        catch (IndexOutOfBoundsException e) {
            return pendencias;
        }
    }

    public List<Pendencia> listArquivos(Automacao automacao) throws IOException {
        List<Pendencia> pendencias = new ArrayList<>();
        List<String> arquivos = fileUtil.listarArquivos(fileUtil.getArquivosPendenciasPath() + automacao.getId() + "/");
        for (String arquivo : arquivos) {
            Pendencia pendencia = new Pendencia();
            pendencia.setAutomacao(automacao);
            pendencia.setNomeArquivo(arquivo);
            pendencias.add(pendencia);
        }

        return pendencias;
    }

    public Pendencia get(Integer id) throws PendenciaNaoEncontradaException {
        Optional<Pendencia> pendenciaOptional = pendenciaRepository.findById(id);
        if (pendenciaOptional.isPresent()) {
            return pendenciaOptional.get();
        }
        else {
            throw new PendenciaNaoEncontradaException(id);
        }
    }

    public void delete(Integer id) throws PendenciaNaoEncontradaException {
        pendenciaRepository.delete(get(id));
    }

    public void deleteByAutomacao(Automacao automacao, boolean apagarApenasProcessadas) {
        List<Pendencia> pendencias = listAll(automacao);
        if (apagarApenasProcessadas) {
            pendencias.removeIf(pendencia -> !pendencia.isProcessado().equalsIgnoreCase("sim"));
        }
        pendenciaRepository.deleteAll(pendencias);
    }
}
