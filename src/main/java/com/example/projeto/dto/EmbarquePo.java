package com.example.projeto.dto;

import com.example.projeto.model.FichaTecnica;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Uma linha do resumo de embarque da aba "Embarque por PO": todos os itens de um mesmo
 * número de pedido agrupados, com os dados de navio e as datas de saída e chegada.
 *
 * É um POJO com getters (e não um record) porque o Thymeleaf/SpEL resolve propriedade
 * por getX(), que é o padrão já usado pelas outras telas.
 *
 * Um PO pode ter mais de um navio quando o pedido é embarcado em partes. Nesse caso a
 * linha mostra a menor data de saída e a maior data de chegada — o intervalo real do
 * pedido inteiro — e a lista de itens abre o detalhe navio a navio.
 */
public class EmbarquePo {

    private String numeroPedido;
    private String fornecedores;
    private String colecoes;
    private String navios;
    private int qtdNavios;
    private LocalDate saida;
    private LocalDate chegada;
    private Integer diasTransito;
    private Integer diasParaChegar;
    private int itens;
    private int itensCancelados;
    private String qtdResumo;
    private Double valorUsd;
    private String situacao;
    private String situacaoLabel;
    private String situacaoCor;
    private String statusPedidos;
    private List<FichaTecnica> itensLista = new ArrayList<>();

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getFornecedores() {
        return fornecedores;
    }

    public void setFornecedores(String fornecedores) {
        this.fornecedores = fornecedores;
    }

    public String getColecoes() {
        return colecoes;
    }

    public void setColecoes(String colecoes) {
        this.colecoes = colecoes;
    }

    public String getNavios() {
        return navios;
    }

    public void setNavios(String navios) {
        this.navios = navios;
    }

    public int getQtdNavios() {
        return qtdNavios;
    }

    public void setQtdNavios(int qtdNavios) {
        this.qtdNavios = qtdNavios;
    }

    public LocalDate getSaida() {
        return saida;
    }

    public void setSaida(LocalDate saida) {
        this.saida = saida;
    }

    public LocalDate getChegada() {
        return chegada;
    }

    public void setChegada(LocalDate chegada) {
        this.chegada = chegada;
    }

    public Integer getDiasTransito() {
        return diasTransito;
    }

    public void setDiasTransito(Integer diasTransito) {
        this.diasTransito = diasTransito;
    }

    /** Dias entre hoje e a chegada. Negativo quando a data de chegada já passou. */
    public Integer getDiasParaChegar() {
        return diasParaChegar;
    }

    public void setDiasParaChegar(Integer diasParaChegar) {
        this.diasParaChegar = diasParaChegar;
    }

    public int getItens() {
        return itens;
    }

    public void setItens(int itens) {
        this.itens = itens;
    }

    public int getItensCancelados() {
        return itensCancelados;
    }

    public void setItensCancelados(int itensCancelados) {
        this.itensCancelados = itensCancelados;
    }

    /**
     * Quantidade comprada já formatada e separada por unidade ("1.200,00 MT · 80,00 KG").
     * O texto é montado no service porque metro, quilo e unidade não se somam num número
     * só — e porque o Thymeleaf não resolve mapa aninhado com chave vinda de th:each.
     */
    public String getQtdResumo() {
        return qtdResumo;
    }

    public void setQtdResumo(String qtdResumo) {
        this.qtdResumo = qtdResumo;
    }

    public Double getValorUsd() {
        return valorUsd;
    }

    public void setValorUsd(Double valorUsd) {
        this.valorUsd = valorUsd;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getSituacaoLabel() {
        return situacaoLabel;
    }

    public void setSituacaoLabel(String situacaoLabel) {
        this.situacaoLabel = situacaoLabel;
    }

    public String getSituacaoCor() {
        return situacaoCor;
    }

    public void setSituacaoCor(String situacaoCor) {
        this.situacaoCor = situacaoCor;
    }

    public String getStatusPedidos() {
        return statusPedidos;
    }

    public void setStatusPedidos(String statusPedidos) {
        this.statusPedidos = statusPedidos;
    }

    public List<FichaTecnica> getItensLista() {
        return itensLista;
    }

    public void setItensLista(List<FichaTecnica> itensLista) {
        this.itensLista = itensLista;
    }
}
