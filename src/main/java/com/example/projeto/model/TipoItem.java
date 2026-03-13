package com.example.projeto.model;

public enum TipoItem {
    TECIDO("Tecido"),
    ACESSORIO("Acessório");

    private final String descricao;

    TipoItem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
