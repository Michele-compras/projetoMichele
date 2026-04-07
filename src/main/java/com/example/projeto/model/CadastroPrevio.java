package com.example.projeto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cadastro_previo")
public class CadastroPrevio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marca;

    private String colecao;

    @Enumerated(EnumType.STRING)
    private TipoItem insumo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getColecao() { return colecao; }
    public void setColecao(String colecao) { this.colecao = colecao; }

    public TipoItem getInsumo() { return insumo; }
    public void setInsumo(TipoItem insumo) { this.insumo = insumo; }
}
