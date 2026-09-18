package com.example.projeto.controller;

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
import java.util.List;
import java.util.Map;

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
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            Model model) {

        List<FichaTecnica> fichas;
        boolean temFiltro = colecao != null || tipo != null || codigo != null || numeroPedido != null || fornecedor != null
                || statusPedido != null || dataInicio != null || dataFim != null;

        if (temFiltro) {
            fichas = service.buscarComFiltros(colecao, tipo, statusPedido, dataInicio, dataFim, null, null, codigo, numeroPedido, fornecedor);
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
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
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

        // URL desta tela com os filtros aplicados: enviada nos links Ver/Editar para que,
        // ao salvar a ficha, o sistema volte para cá em vez de cair em /fichas sem filtro.
        model.addAttribute("urlRetorno", montarUrlRetorno(colecao, tipo, statusPedido, codigo,
                numeroPedido, fornecedor, dataInicio, dataFim));
        return "pedidos/lista";
    }

    /** Monta "/pedidos?..." com os filtros preenchidos, ignorando os vazios. */
    private String montarUrlRetorno(String colecao, String tipo, StatusPedido statusPedido, String codigo,
                                    String numeroPedido, String fornecedor,
                                    LocalDate dataInicio, LocalDate dataFim) {
        List<String> partes = new ArrayList<>();
        adicionarFiltro(partes, "colecao", colecao);
        adicionarFiltro(partes, "tipo", tipo);
        adicionarFiltro(partes, "statusPedido", statusPedido != null ? statusPedido.name() : null);
        adicionarFiltro(partes, "codigo", codigo);
        adicionarFiltro(partes, "numeroPedido", numeroPedido);
        adicionarFiltro(partes, "fornecedor", fornecedor);
        adicionarFiltro(partes, "dataInicio", dataInicio != null ? dataInicio.toString() : null);
        adicionarFiltro(partes, "dataFim", dataFim != null ? dataFim.toString() : null);
        return "/pedidos" + (partes.isEmpty() ? "" : "?" + String.join("&", partes));
    }

    private void adicionarFiltro(List<String> partes, String nome, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(nome + "=" + URLEncoder.encode(valor, StandardCharsets.UTF_8));
        }
    }
}
