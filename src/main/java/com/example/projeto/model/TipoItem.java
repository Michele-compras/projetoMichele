package com.example.projeto.model;

public enum TipoItem {
    TECIDO("Tecido"),
    ACESSORIO_METRO("Aviamento em Metro"),
    ACESSORIO_UNIDADE("Aviamento em Unidade");

    private final String descricao;

    TipoItem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
