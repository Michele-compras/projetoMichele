package com.example.projeto.model;

public enum TipoItem {
    TECIDO("Tecido"),
    ACESSORIO("Acessório"),
    ACESSORIO_METRO("Acessório em Metro"),
    ACESSORIO_UNIDADE("Acessório em Unidade");

    private final String descricao;

    TipoItem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
