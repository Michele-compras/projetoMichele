package com.example.projeto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bandeira_novidade",
       uniqueConstraints = @UniqueConstraint(columnNames = {"colecao", "fornecedor", "insumo"}))
public class BandeiraNovidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String colecao;
    private String fornecedor;
    private String insumo;
    private Integer quantidade;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getColecao() { return colecao; }
    public void setColecao(String colecao) { this.colecao = colecao; }

    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }

    public String getInsumo() { return insumo; }
    public void setInsumo(String insumo) { this.insumo = insumo; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}
