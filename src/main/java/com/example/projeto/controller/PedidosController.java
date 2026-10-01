package com.example.projeto.controller;

import com.example.projeto.dto.EmbarquePo;
import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.repository.ColecaoRepository;
import com.example.projeto.repository.FornecedorRepository;
import com.example.projeto.service.FichaTecnicaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/pedidos")
public class PedidosController {

    private final FichaTecnicaService service;
    private final ColecaoRepository colecaoRepo;
    private final FornecedorRepository fornecedorRepo;

    public PedidosController(FichaTecnicaService service, ColecaoRepository colecaoRepo,
                             FornecedorRepository fornecedorRepo) {
        this.service = service;
        this.colecaoRepo = colecaoRepo;
        this.fornecedorRepo = fornecedorRepo;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String colecao,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) StatusPedido statusPedido,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String numeroPedido,
            @RequestParam(required = false) String fornecedor,
            // ── Filtros da aba "Embarque por PO" ──────────────────────────────
            @RequestParam(required = false) String navio,
            @RequestParam(required = false) String situacaoEmbarque,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate saidaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate saidaFim,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate chegadaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate chegadaFim,
            // Aba aberta ao carregar a tela: o formulário de filtros é um só e devolve
            // para a aba de onde o filtro foi aplicado, em vez de voltar sempre na primeira.
            @RequestParam(required = false, defaultValue = "itens") String aba,
            Model model) {

        List<FichaTecnica> fichas;
        boolean temFiltro = colecao != null || tipo != null || codigo != null || numeroPedido != null || fornecedor != null
                || statusPedido != null;

        if (temFiltro) {
            fichas = service.buscarComFiltros(colecao, tipo, statusPedido, null, null, null, null, codigo, numeroPedido, fornecedor);
        } else {
            fichas = service.listarTodas();
        }

        model.addAttribute("fichas", fichas);
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("colecoesCadastradas", colecaoRepo.findAll());
        model.addAttribute("fornecedoresCadastrados", fornecedorRepo.findAll());
        model.addAttribute("colecaoFiltro", colecao);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("statusPedidoSelecionado", statusPedido);
        model.addAttribute("codigoFiltro", codigo);
        model.addAttribute("numeroPedidoFiltro", numeroPedido);
        model.addAttribute("fornecedorFiltro", fornecedor);
        model.addAttribute("qtdPorColecao", service.qtdPorColecao());

        // ── Total comprado da listagem ────────────────────────────────────────
        // Soma sobre as fichas que estão na tela, então o total acompanha o filtro
        // sozinho: sem filtro é o geral, com filtro é o do recorte.
        // Somado por insumo, e não num número só, porque metro, quilo e unidade não
        // se somam. Itens cancelados ficam de fora, mesma regra de /quadro-compras
        // e dos gráficos, para os números não se contradizerem entre as telas.
        Map<String, Double> totalCompradoPorInsumo = new LinkedHashMap<>();
        java.util.Set<String> unidades = new java.util.LinkedHashSet<>();
        double totalQuantidade = 0;
        int itensSomados = 0, itensCancelados = 0;
        for (FichaTecnica f : fichas) {
            if (f.isCancelado()) {
                itensCancelados++;
                continue;
            }
            if (f.getQuantidadeComprada() == null || f.getTipo() == null) continue;
            totalCompradoPorInsumo.merge(f.getTipo(), f.getQuantidadeComprada(), Double::sum);
            totalQuantidade += f.getQuantidadeComprada();
            if (f.getUnidadeMedida() != null && !f.getUnidadeMedida().isBlank()) {
                unidades.add(f.getUnidadeMedida());
            }
            itensSomados++;
        }
        model.addAttribute("totalCompradoPorInsumo", totalCompradoPorInsumo);
        // Soma da coluna Qtd. da listagem.
        model.addAttribute("totalQuantidade", totalQuantidade);
        // Com mais de uma unidade no recorte, o total geral soma metro com unidade
        // e com quilo. O número é mostrado assim mesmo, mas avisando na tela.
        model.addAttribute("unidadesMisturadas", unidades.size() > 1);
        model.addAttribute("unidadesListadas", String.join(", ", unidades));
        model.addAttribute("itensSomados", itensSomados);
        model.addAttribute("itensCancelados", itensCancelados);
        model.addAttribute("temFiltro", temFiltro);

        // ── Aba "Embarque por PO" ─────────────────────────────────────────────
        // Parte da mesma lista já filtrada acima, para as duas abas falarem do mesmo
        // recorte, e só então aplica os filtros próprios de embarque (navio e datas),
        // que valem apenas nesta aba.
        List<FichaTecnica> fichasEmbarque = filtrarPorEmbarque(fichas, navio, saidaInicio, saidaFim,
                chegadaInicio, chegadaFim);
        List<EmbarquePo> embarques = service.resumoEmbarquePorPo(fichasEmbarque);
        if (situacaoEmbarque != null && !situacaoEmbarque.isBlank()) {
            embarques = embarques.stream()
                    .filter(e -> situacaoEmbarque.equals(e.getSituacao()))
                    .toList();
        }

        boolean temFiltroEmbarque = (navio != null && !navio.isBlank())
                || (situacaoEmbarque != null && !situacaoEmbarque.isBlank())
                || saidaInicio != null || saidaFim != null
                || chegadaInicio != null || chegadaFim != null;

        // Contadores do cabeçalho da aba. Contam POs, não itens: a pergunta da tela é
        // quantos pedidos estão em cada etapa do embarque.
        Map<String, Long> contagemSituacao = new LinkedHashMap<>();
        for (String chave : List.of("PREVISTO", "EM_TRANSITO", "CHEGADO", "SEM_DATA")) {
            contagemSituacao.put(chave, embarques.stream().filter(e -> chave.equals(e.getSituacao())).count());
        }

        model.addAttribute("embarques", embarques);
        model.addAttribute("totalPos", embarques.size());
        model.addAttribute("qtdPrevisto", contagemSituacao.get("PREVISTO"));
        model.addAttribute("qtdEmTransito", contagemSituacao.get("EM_TRANSITO"));
        model.addAttribute("qtdChegado", contagemSituacao.get("CHEGADO"));
        model.addAttribute("qtdSemData", contagemSituacao.get("SEM_DATA"));
        model.addAttribute("naviosCadastrados", naviosDisponiveis(fichas));
        model.addAttribute("navioFiltro", navio);
        model.addAttribute("situacaoEmbarqueFiltro", situacaoEmbarque);
        model.addAttribute("saidaInicio", saidaInicio);
        model.addAttribute("saidaFim", saidaFim);
        model.addAttribute("chegadaInicio", chegadaInicio);
        model.addAttribute("chegadaFim", chegadaFim);
        model.addAttribute("temFiltroEmbarque", temFiltroEmbarque);
        model.addAttribute("abaAtiva", "embarque".equals(aba) ? "embarque" : "itens");

        // URL desta tela com os filtros aplicados: enviada nos links Ver/Editar para que,
        // ao salvar a ficha, o sistema volte para cá em vez de cair em /fichas sem filtro.
        model.addAttribute("urlRetorno", montarUrlRetorno(colecao, tipo, statusPedido, codigo,
                numeroPedido, fornecedor, navio, situacaoEmbarque,
                saidaInicio, saidaFim, chegadaInicio, chegadaFim, aba));
        return "pedidos/lista";
    }

    /**
     * Aplica os filtros de embarque item a item, antes do agrupamento por PO.
     * Filtro de data preenchido descarta o item sem a data correspondente: quem procura
     * "saiu em março" não quer no meio do resultado o item que ainda não tem embarque.
     */
    private List<FichaTecnica> filtrarPorEmbarque(List<FichaTecnica> fichas, String navio,
                                                  LocalDate saidaInicio, LocalDate saidaFim,
                                                  LocalDate chegadaInicio, LocalDate chegadaFim) {
        String navioBusca = (navio == null || navio.isBlank()) ? null : navio.trim().toLowerCase();
        List<FichaTecnica> resultado = new ArrayList<>();
        for (FichaTecnica f : fichas) {
            if (navioBusca != null) {
                String nome = f.getNomeNavio();
                if (nome == null || !nome.toLowerCase().contains(navioBusca)) continue;
            }
            if (!dentroDoPeriodo(f.getDataSaidaOrigem(), saidaInicio, saidaFim)) continue;
            if (!dentroDoPeriodo(f.getDataChegadaDestino(), chegadaInicio, chegadaFim)) continue;
            resultado.add(f);
        }
        return resultado;
    }

    private boolean dentroDoPeriodo(LocalDate data, LocalDate inicio, LocalDate fim) {
        if (inicio == null && fim == null) return true;
        if (data == null) return false;
        if (inicio != null && data.isBefore(inicio)) return false;
        return fim == null || !data.isAfter(fim);
    }

    /** Navios que aparecem no recorte atual, para o filtro virar uma lista em vez de digitação. */
    private Set<String> naviosDisponiveis(List<FichaTecnica> fichas) {
        Set<String> navios = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (FichaTecnica f : fichas) {
            if (f.getNomeNavio() != null && !f.getNomeNavio().isBlank()) {
                navios.add(f.getNomeNavio().trim());
            }
        }
        return new LinkedHashSet<>(navios);
    }

    /** Monta "/pedidos?..." com os filtros preenchidos, ignorando os vazios. */
    private String montarUrlRetorno(String colecao, String tipo, StatusPedido statusPedido, String codigo,
                                    String numeroPedido, String fornecedor,
                                    String navio, String situacaoEmbarque,
                                    LocalDate saidaInicio, LocalDate saidaFim,
                                    LocalDate chegadaInicio, LocalDate chegadaFim, String aba) {
        List<String> partes = new ArrayList<>();
        adicionarFiltro(partes, "colecao", colecao);
        adicionarFiltro(partes, "tipo", tipo);
        adicionarFiltro(partes, "statusPedido", statusPedido != null ? statusPedido.name() : null);
        adicionarFiltro(partes, "codigo", codigo);
        adicionarFiltro(partes, "numeroPedido", numeroPedido);
        adicionarFiltro(partes, "fornecedor", fornecedor);
        adicionarFiltro(partes, "navio", navio);
        adicionarFiltro(partes, "situacaoEmbarque", situacaoEmbarque);
        adicionarFiltro(partes, "saidaInicio", saidaInicio != null ? saidaInicio.toString() : null);
        adicionarFiltro(partes, "saidaFim", saidaFim != null ? saidaFim.toString() : null);
        adicionarFiltro(partes, "chegadaInicio", chegadaInicio != null ? chegadaInicio.toString() : null);
        adicionarFiltro(partes, "chegadaFim", chegadaFim != null ? chegadaFim.toString() : null);
        adicionarFiltro(partes, "aba", "embarque".equals(aba) ? aba : null);
        return "/pedidos" + (partes.isEmpty() ? "" : "?" + String.join("&", partes));
    }

    private void adicionarFiltro(List<String> partes, String nome, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(nome + "=" + URLEncoder.encode(valor, StandardCharsets.UTF_8));
        }
    }
}
