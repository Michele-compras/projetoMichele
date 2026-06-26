package com.example.projeto.model;

public enum StatusAmostra {
    PENDENTE("Aguardando Amostra"),
    EM_ANALISE("Em Análise"),
    APROVADO("Aprovada"),
    REPROVADO("Reprovada"),
    CANCELADO("Cancelada");

    private final String descricao;

    StatusAmostra(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
