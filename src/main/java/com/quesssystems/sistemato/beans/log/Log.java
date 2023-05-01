package com.quesssystems.sistemato.beans.log;

import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.beans.token.Token;
import util.ConversorUtil;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;

@Entity
@Table(name = "log")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="id_automacao", nullable=false)
    private Automacao automacao;

    @ManyToOne
    @JoinColumn(name="id_token")
    private Token token;

    @Column(nullable = false)
    private Timestamp hora;

    @Column(nullable = false)
    private String mensagem;

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

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getHora() {
        long timestamp = hora.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        return ConversorUtil.getDateToString(cal, "dd/MM/yyyy HH:mm:ss");
    }

    public void setHora(Timestamp hora) {
        this.hora = hora;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
