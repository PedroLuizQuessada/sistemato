package com.quesssystems.sistemato.beans.token;

import com.quesssystems.sistemato.beans.log.Log;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "token")
public class Token {
    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private boolean ativo;

    @OneToMany(mappedBy = "token")
    private Set<Log> logs;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Set<Log> getLogs() {
        return logs;
    }

    public void setLogs(Set<Log> logs) {
        this.logs = logs;
    }
}
