package com.example.projeto.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ficha_tecnica")
public class FichaTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoItem tipo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private String cor;

    private String composicao;

    private String gramatura;

    private String fotoPath;

    private String estagio;

    private String tipoSolicitacao;

    private String codigo;

    private String fornecedor;

    private String referencia;

    private String refFornecedor;

    private String categoriaProduto;

    private String marca;

    private String marcaQueComprou;

    private String marcaQueUtiliza;

    private String largura;

    private Double minimoCompra;

    private Double precoUsd;

    private Double precoReais;

    @Enumerated(EnumType.STRING)
    private StatusPedido statusPedido;

    private String colecao;

    private String statusColecao;

    private Double minimo;

    private Double quantidadeComprada;

    private String numeroPedido;

    private LocalDate dataColocacaoPedido;

    private String nomeNavio;

    private LocalDate dataSaidaOrigem;

    private LocalDate dataChegadaDestino;

    @Enumerated(EnumType.STRING)
    private StatusAmostra statusAmostraCor;

    private LocalDate dataAmostraCor;

    private LocalDate dataAprovacaoAmostraCor;

    @Lob
    private String observacaoAmostraCor;

    @Enumerated(EnumType.STRING)
    private StatusAmostra statusAmostraProducao;

    private LocalDate dataAmostraProducao;

    private LocalDate dataAprovacaoAmostraProducao;

    @Lob
    private String observacaoAmostraProducao;

    @PrePersist
    public void prePersist() {
        if (statusAmostraCor == null) {
            statusAmostraCor = StatusAmostra.PENDENTE;
        }
        if (statusAmostraProducao == null) {
            statusAmostraProducao = StatusAmostra.PENDENTE;
        }
        if (statusPedido == null) {
            statusPedido = StatusPedido.EM_ANDAMENTO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoItem getTipo() {
        return tipo;
    }

    public void setTipo(TipoItem tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getComposicao() {
        return composicao;
    }

    public void setComposicao(String composicao) {
        this.composicao = composicao;
    }

    public String getGramatura() {
        return gramatura;
    }

    public void setGramatura(String gramatura) {
        this.gramatura = gramatura;
    }

    public Double getMinimo() {
        return minimo;
    }

    public void setMinimo(Double minimo) {
        this.minimo = minimo;
    }

    public Double getQuantidadeComprada() {
        return quantidadeComprada;
    }

    public void setQuantidadeComprada(Double quantidadeComprada) {
        this.quantidadeComprada = quantidadeComprada;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public LocalDate getDataColocacaoPedido() {
        return dataColocacaoPedido;
    }

    public void setDataColocacaoPedido(LocalDate dataColocacaoPedido) {
        this.dataColocacaoPedido = dataColocacaoPedido;
    }

    public String getNomeNavio() {
        return nomeNavio;
    }

    public void setNomeNavio(String nomeNavio) {
        this.nomeNavio = nomeNavio;
    }

    public LocalDate getDataSaidaOrigem() {
        return dataSaidaOrigem;
    }

    public void setDataSaidaOrigem(LocalDate dataSaidaOrigem) {
        this.dataSaidaOrigem = dataSaidaOrigem;
    }

    public LocalDate getDataChegadaDestino() {
        return dataChegadaDestino;
    }

    public void setDataChegadaDestino(LocalDate dataChegadaDestino) {
        this.dataChegadaDestino = dataChegadaDestino;
    }

    public StatusAmostra getStatusAmostraCor() {
        return statusAmostraCor;
    }

    public void setStatusAmostraCor(StatusAmostra statusAmostraCor) {
        this.statusAmostraCor = statusAmostraCor;
    }

    public LocalDate getDataAmostraCor() {
        return dataAmostraCor;
    }

    public void setDataAmostraCor(LocalDate dataAmostraCor) {
        this.dataAmostraCor = dataAmostraCor;
    }

    public LocalDate getDataAprovacaoAmostraCor() {
        return dataAprovacaoAmostraCor;
    }

    public void setDataAprovacaoAmostraCor(LocalDate dataAprovacaoAmostraCor) {
        this.dataAprovacaoAmostraCor = dataAprovacaoAmostraCor;
    }

    public String getObservacaoAmostraCor() {
        return observacaoAmostraCor;
    }

    public void setObservacaoAmostraCor(String observacaoAmostraCor) {
        this.observacaoAmostraCor = observacaoAmostraCor;
    }

    public StatusAmostra getStatusAmostraProducao() {
        return statusAmostraProducao;
    }

    public void setStatusAmostraProducao(StatusAmostra statusAmostraProducao) {
        this.statusAmostraProducao = statusAmostraProducao;
    }

    public LocalDate getDataAmostraProducao() {
        return dataAmostraProducao;
    }

    public void setDataAmostraProducao(LocalDate dataAmostraProducao) {
        this.dataAmostraProducao = dataAmostraProducao;
    }

    public LocalDate getDataAprovacaoAmostraProducao() {
        return dataAprovacaoAmostraProducao;
    }

    public void setDataAprovacaoAmostraProducao(LocalDate dataAprovacaoAmostraProducao) {
        this.dataAprovacaoAmostraProducao = dataAprovacaoAmostraProducao;
    }

    public String getObservacaoAmostraProducao() {
        return observacaoAmostraProducao;
    }

    public void setObservacaoAmostraProducao(String observacaoAmostraProducao) {
        this.observacaoAmostraProducao = observacaoAmostraProducao;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }

    public String getCategoriaProduto() {
        return categoriaProduto;
    }

    public void setCategoriaProduto(String categoriaProduto) {
        this.categoriaProduto = categoriaProduto;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getLargura() {
        return largura;
    }

    public void setLargura(String largura) {
        this.largura = largura;
    }

    public Double getMinimoCompra() {
        return minimoCompra;
    }

    public void setMinimoCompra(Double minimoCompra) {
        this.minimoCompra = minimoCompra;
    }

    public Double getPrecoUsd() {
        return precoUsd;
    }

    public void setPrecoUsd(Double precoUsd) {
        this.precoUsd = precoUsd;
    }

    public Double getPrecoReais() {
        return precoReais;
    }

    public void setPrecoReais(Double precoReais) {
        this.precoReais = precoReais;
    }

    public StatusPedido getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(StatusPedido statusPedido) {
        this.statusPedido = statusPedido;
    }

    public String getColecao() {
        return colecao;
    }

    public void setColecao(String colecao) {
        this.colecao = colecao;
    }

    public String getStatusColecao() {
        return statusColecao;
    }

    public void setStatusColecao(String statusColecao) {
        this.statusColecao = statusColecao;
    }

    public String getEstagio() {
        return estagio;
    }

    public void setEstagio(String estagio) {
        this.estagio = estagio;
    }

    public String getTipoSolicitacao() {
        return tipoSolicitacao;
    }

    public void setTipoSolicitacao(String tipoSolicitacao) {
        this.tipoSolicitacao = tipoSolicitacao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getRefFornecedor() {
        return refFornecedor;
    }

    public void setRefFornecedor(String refFornecedor) {
        this.refFornecedor = refFornecedor;
    }

    public String getMarcaQueComprou() {
        return marcaQueComprou;
    }

    public void setMarcaQueComprou(String marcaQueComprou) {
        this.marcaQueComprou = marcaQueComprou;
    }

    public String getMarcaQueUtiliza() {
        return marcaQueUtiliza;
    }

    public void setMarcaQueUtiliza(String marcaQueUtiliza) {
        this.marcaQueUtiliza = marcaQueUtiliza;
    }
}
