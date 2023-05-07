package com.quesssystems.sistemato.beans.pendencia;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import util.ConversorUtil;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;

@Entity
@Table(name = "pendencia")
public class Pendencia {
    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name="id_automacao", nullable=false)
    private Automacao automacao;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(columnDefinition = "TEXT")
    private String json;

    @Column()
    private boolean processado;

    @Column(name = "data_hora_upload", nullable = false)
    private Timestamp dataHoraUpload;

    @Column(name = "data_hora_processamento")
    private Timestamp dataHoraProcessamento;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Automacao getAutomacao() {
        return automacao;
    }

    public void setAutomacao(Automacao automacao) {
        this.automacao = automacao;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String isProcessado() {
        if (processado) {
            return "Sim";
        }
        return "Não";
    }

    public void setProcessado(boolean processado) {
        this.processado = processado;
    }

    public String getDataHoraUpload() {
        long timestamp = dataHoraUpload.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        return ConversorUtil.getDateToString(cal, "dd/MM/yyyy HH:mm:ss");
    }

    public void setDataHoraUpload(Timestamp dataHoraUpload) {
        this.dataHoraUpload = dataHoraUpload;
    }

    public String getDataHoraProcessamento() {
        if (dataHoraProcessamento != null) {
            long timestamp = dataHoraProcessamento.getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);

            return ConversorUtil.getDateToString(cal, "dd/MM/yyyy HH:mm:ss");
        }
        else {
            return "";
        }
    }

    public void setDataHoraProcessamento(Timestamp dataHoraProcessamento) {
        this.dataHoraProcessamento = dataHoraProcessamento;
    }
}
