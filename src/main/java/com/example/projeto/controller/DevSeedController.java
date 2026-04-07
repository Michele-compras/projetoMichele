package com.example.projeto.controller;

import com.example.projeto.model.*;
import com.example.projeto.repository.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DevSeedController {

    private final FichaTecnicaRepository fichaRepo;
    private final ColecaoRepository colecaoRepo;
    private final InsumoRepository insumoRepo;
    private final MarcaRepository marcaRepo;

    public DevSeedController(FichaTecnicaRepository fichaRepo,
                             ColecaoRepository colecaoRepo,
                             InsumoRepository insumoRepo,
                             MarcaRepository marcaRepo) {
        this.fichaRepo   = fichaRepo;
        this.colecaoRepo = colecaoRepo;
        this.insumoRepo  = insumoRepo;
        this.marcaRepo   = marcaRepo;
    }

    @GetMapping("/dev/seed")
    public String seed(RedirectAttributes ra) {

        // ── Garantir cadastros prévios ──────────────────────────
        ensureColecao("Inverno 2025");
        ensureColecao("Verão 2025");
        ensureColecao("Inverno 2026");

        ensureInsumo("Tecido");
        ensureInsumo("Aviamento em Metro");
        ensureInsumo("Aviamento em Unidade");

        ensureMarca("ANIME");
        ensureMarca("MOMI");
        ensureMarca("AUTHORIA");
        ensureMarca("BIMBI");
        ensureMarca("YOUCCIE");

        // ── Fichas fictícias ────────────────────────────────────
        ficha("Inverno 2025", "Tecido",               "Fornecedor Alpha",  "Cor Azul Marinho",  "ANIME",   "TEX-001", "FA-001", 250.0, 12.5, 85.0,  95.0,  StatusPedido.EMBARCADO,  StatusAmostra.APROVADO,  StatusAmostra.PENDENTE,  LocalDate.of(2024,9,10),  LocalDate.of(2024,10,5));
        ficha("Inverno 2025", "Tecido",               "Fornecedor Alpha",  "Cor Cinza",         "MOMI",    "TEX-002", "FA-002", 180.0, 12.5, 85.0,  95.0,  StatusPedido.RECEBIDO,   StatusAmostra.APROVADO,  StatusAmostra.APROVADO,  LocalDate.of(2024,9,12),  LocalDate.of(2024,10,8));
        ficha("Inverno 2025", "Aviamento em Metro",   "Fornecedor Beta",   "Elástico 3cm",      "ANIME",   "AVM-001", "FB-010", 500.0,  2.8,  4.5,  null,  StatusPedido.RECEBIDO,   StatusAmostra.APROVADO,  StatusAmostra.APROVADO,  LocalDate.of(2024,9,15),  LocalDate.of(2024,10,10));
        ficha("Inverno 2025", "Aviamento em Metro",   "Fornecedor Beta",   "Viés Preto",        "BIMBI",   "AVM-002", "FB-011", 300.0,  1.2,  2.0,  null,  StatusPedido.EMBARCADO,  StatusAmostra.APROVADO,  StatusAmostra.PENDENTE,  LocalDate.of(2024,9,18),  null);
        ficha("Inverno 2025", "Aviamento em Unidade", "Fornecedor Gama",   "Botão 4 Furos",     "AUTHORIA","AVU-001", "FG-020", 800.0,  0.3,  0.5,  null,  StatusPedido.RECEBIDO,   StatusAmostra.APROVADO,  StatusAmostra.APROVADO,  LocalDate.of(2024,9,20),  LocalDate.of(2024,10,15));
        ficha("Inverno 2025", "Aviamento em Unidade", "Fornecedor Gama",   "Zíper 20cm",        "MOMI",    "AVU-002", "FG-021", 400.0,  1.5,  2.2,  null,  StatusPedido.EM_ANDAMENTO,StatusAmostra.PENDENTE, StatusAmostra.PENDENTE,  null,                     null);

        ficha("Verão 2025",  "Tecido",               "Fornecedor Alpha",  "Malha Branca",      "YOUCCIE", "TEX-010", "FA-010", 320.0, 11.0, 78.0,  88.0,  StatusPedido.RECEBIDO,   StatusAmostra.APROVADO,  StatusAmostra.APROVADO,  LocalDate.of(2024,3,5),   LocalDate.of(2024,4,2));
        ficha("Verão 2025",  "Tecido",               "Fornecedor Delta",  "Linho Bege",        "ANIME",   "TEX-011", "FD-001", 150.0, 14.0, 92.0, 102.0, StatusPedido.RECEBIDO,   StatusAmostra.APROVADO,  StatusAmostra.APROVADO,  LocalDate.of(2024,3,8),   LocalDate.of(2024,4,5));
        ficha("Verão 2025",  "Tecido",               "Fornecedor Delta",  "Viscose Floral",    "MOMI",    "TEX-012", "FD-002", 200.0, 13.5, 89.0,  99.0,  StatusPedido.EMBARCADO,  StatusAmostra.APROVADO,  StatusAmostra.PENDENTE,  LocalDate.of(2024,3,10),  null);
        ficha("Verão 2025",  "Aviamento em Metro",   "Fornecedor Beta",   "Renda 5cm",         "BIMBI",   "AVM-010", "FB-020", 600.0,  3.5,  5.8,  null,  StatusPedido.RECEBIDO,   StatusAmostra.APROVADO,  StatusAmostra.APROVADO,  LocalDate.of(2024,3,12),  LocalDate.of(2024,4,10));
        ficha("Verão 2025",  "Aviamento em Unidade", "Fornecedor Gama",   "Botão Colorido",    "YOUCCIE", "AVU-010", "FG-030", 1200.0, 0.2,  0.4,  null,  StatusPedido.RECEBIDO,   StatusAmostra.APROVADO,  StatusAmostra.APROVADO,  LocalDate.of(2024,3,15),  LocalDate.of(2024,4,12));
        ficha("Verão 2025",  "Aviamento em Unidade", "Fornecedor Epsilon","Passador Plástico",  "AUTHORIA","AVU-011", "FE-001", 500.0,  0.5,  0.8,  null,  StatusPedido.EM_ANDAMENTO,StatusAmostra.PENDENTE, StatusAmostra.PENDENTE,  null,                     null);

        ficha("Inverno 2026", "Tecido",              "Fornecedor Alpha",  "Moletom Preto",     "BIMBI",   "TEX-020", "FA-020", 400.0, 18.0, 125.0,138.0, StatusPedido.EM_ANDAMENTO,StatusAmostra.PENDENTE, StatusAmostra.PENDENTE,  LocalDate.of(2025,9,1),   null);
        ficha("Inverno 2026", "Tecido",              "Fornecedor Delta",  "Tweed Marrom",      "MOMI",    "TEX-021", "FD-010", 130.0, 22.0, 155.0,170.0, StatusPedido.EM_ANDAMENTO,StatusAmostra.PENDENTE, StatusAmostra.PENDENTE,  LocalDate.of(2025,9,5),   null);
        ficha("Inverno 2026", "Aviamento em Metro",  "Fornecedor Beta",   "Fita Veludo 2cm",   "ANIME",   "AVM-020", "FB-030", 700.0,  2.2,  3.8,  null, StatusPedido.EM_ANDAMENTO,StatusAmostra.PENDENTE, StatusAmostra.PENDENTE,  LocalDate.of(2025,9,8),   null);
        ficha("Inverno 2026", "Aviamento em Unidade","Fornecedor Gama",   "Botão Madrepérola", "YOUCCIE", "AVU-020", "FG-040", 600.0,  0.8,  1.2,  null, StatusPedido.EM_ANDAMENTO,StatusAmostra.PENDENTE, StatusAmostra.PENDENTE,  LocalDate.of(2025,9,10),  null);

        ra.addFlashAttribute("mensagem", "Dados fictícios inseridos com sucesso!");
        return "redirect:/fichas";
    }

    private void ficha(String colecao, String tipo, String fornecedor, String cor, String marca,
                       String refTex, String refForn, Double qty, Double precoUsd, Double precoR$Min,
                       Double precoR$Max, StatusPedido statusPedido,
                       StatusAmostra statusCor, StatusAmostra statusProd,
                       LocalDate dataPedido, LocalDate dataAprovacaoCor) {
        FichaTecnica f = new FichaTecnica();
        f.setColecao(colecao);
        f.setTipo(tipo);
        f.setFornecedor(fornecedor);
        f.setCor(cor);
        f.setMarca(marca);
        f.setRefTexcotton(refTex);
        f.setRefFornecedor(refForn);
        f.setQuantidadeComprada(qty);
        f.setPrecoUsd(precoUsd);
        f.setPrecoReais(precoR$Min);
        f.setMinimoCompra(50.0);
        f.setStatusPedido(statusPedido);
        f.setStatusAmostraCor(statusCor);
        f.setStatusAmostraProducao(statusProd);
        f.setDataColocacaoPedido(dataPedido);
        f.setDataAprovacaoAmostraCor(dataAprovacaoCor);
        f.setNumeroPedido("PED-" + refTex);
        fichaRepo.save(f);
    }

    private void ensureColecao(String nome) {
        if (colecaoRepo.findAll().stream().noneMatch(c -> c.getNome().equals(nome))) {
            Colecao c = new Colecao(); c.setNome(nome); colecaoRepo.save(c);
        }
    }
    private void ensureInsumo(String nome) {
        if (insumoRepo.findAll().stream().noneMatch(i -> i.getNome().equals(nome))) {
            Insumo i = new Insumo(); i.setNome(nome); insumoRepo.save(i);
        }
    }
    private void ensureMarca(String nome) {
        if (marcaRepo.findAll().stream().noneMatch(m -> m.getNome().equals(nome))) {
            Marca m = new Marca(); m.setNome(nome); marcaRepo.save(m);
        }
    }
}
