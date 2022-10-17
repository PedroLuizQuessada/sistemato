package com.quesssystems.sistemato.beans.execucao;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import util.ConversorUtil;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;

@Entity
@Table(name = "execucao")
public class Execucao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="id_automacao", nullable=false)
    private Automacao automacao;

    @Column(nullable = false)
    private Timestamp horaExecucao;

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

    public String getHoraExecucao() {
        long timestamp = horaExecucao.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        return ConversorUtil.getDateToString(cal, "dd/MM/yyyy HH:mm:ss");
    }

    public void setHoraExecucao(Timestamp horaExecucao) {
        this.horaExecucao = horaExecucao;
    }
}
