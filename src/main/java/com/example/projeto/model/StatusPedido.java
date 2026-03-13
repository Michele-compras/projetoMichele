package com.example.projeto.model;

public enum StatusPedido {
    EM_ANDAMENTO("Em Andamento"),
    EMBARCADO("Embarcado"),
    RECEBIDO("Recebido"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
