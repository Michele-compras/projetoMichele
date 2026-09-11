package com.example.projeto.controller;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusAmostra;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.repository.ColecaoRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/aprovacao-embarque")
public class AprovacaoEmbarqueController {

    private final FichaTecnicaService service;
    private final ColecaoRepository colecaoRepo;

    public AprovacaoEmbarqueController(FichaTecnicaService service, ColecaoRepository colecaoRepo) {
        this.service = service;
        this.colecaoRepo = colecaoRepo;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String colecao,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) StatusPedido statusPedido,
            @RequestParam(required = false) StatusAmostra statusAmostra,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            @RequestParam(required = false) String codigo,
            Model model) {

        List<FichaTecnica> fichas;
        boolean temFiltro = colecao != null || tipo != null
                || statusPedido != null || statusAmostra != null
                || dataInicio != null || dataFim != null
                || (codigo != null && !codigo.isBlank());

        if (temFiltro) {
            fichas = service.buscarComFiltros(colecao, tipo, statusPedido, dataInicio, dataFim, null, null, codigo, null, null);
            if (statusAmostra != null) {
                final StatusAmostra filtroStatus = statusAmostra;
                fichas = fichas.stream()
                        .filter(f -> filtroStatus.equals(f.getStatusAmostraProducao()))
                        .toList();
            }
        } else {
            fichas = service.listarTodas();
        }

        model.addAttribute("fichas", fichas);
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("statusAmostraList", StatusAmostra.values());
        model.addAttribute("colecoesCadastradas", colecaoRepo.findAll());
        model.addAttribute("colecaoFiltro", colecao);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("statusPedidoSelecionado", statusPedido);
        model.addAttribute("statusAmostraSelecionado", statusAmostra);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("codigoFiltro", codigo);
        Map<String, Long> qtdPorColecao = service.qtdPorColecao();
        long totalGeral = qtdPorColecao.values().stream().mapToLong(Long::longValue).sum();
        model.addAttribute("qtdPorColecao", qtdPorColecao);
        model.addAttribute("totalGeral", totalGeral);
        model.addAttribute("qtdStatusProducaoPorColecao", service.qtdStatusProducaoPorColecao());
        model.addAttribute("qtdFornecedorPorColecao", service.qtdFornecedorPorColecao());
        model.addAttribute("totalPorFornecedor", service.totalPorFornecedor());
        List<Map<String, Object>> leadtime = service.leadtimeAprovacaoProducaoPorMarca();
        if (colecao != null && !colecao.isBlank()) {
            final String col = colecao.toLowerCase();
            leadtime = leadtime.stream()
                    .filter(r -> r.get("colecao") != null && ((String) r.get("colecao")).toLowerCase().contains(col))
                    .collect(Collectors.toList());
        }
        model.addAttribute("leadtimePorMarca", leadtime);
        // URL desta tela com os filtros aplicados: enviada nos links Ver/Editar para que,
        // ao salvar a ficha, o sistema volte para cá em vez de cair em /fichas sem filtro.
        model.addAttribute("urlRetorno", montarUrlRetorno(colecao, tipo, statusPedido, statusAmostra,
                dataInicio, dataFim, codigo));
        return "aprovacao/embarque";
    }

    /** Monta "/aprovacao-embarque?..." com os filtros preenchidos, ignorando os vazios. */
    private String montarUrlRetorno(String colecao, String tipo, StatusPedido statusPedido,
                                    StatusAmostra statusAmostra, LocalDate dataInicio,
                                    LocalDate dataFim, String codigo) {
        List<String> partes = new ArrayList<>();
        adicionarFiltro(partes, "colecao", colecao);
        adicionarFiltro(partes, "tipo", tipo);
        adicionarFiltro(partes, "statusPedido", statusPedido != null ? statusPedido.name() : null);
        adicionarFiltro(partes, "statusAmostra", statusAmostra != null ? statusAmostra.name() : null);
        adicionarFiltro(partes, "dataInicio", dataInicio != null ? dataInicio.toString() : null);
        adicionarFiltro(partes, "dataFim", dataFim != null ? dataFim.toString() : null);
        adicionarFiltro(partes, "codigo", codigo);
        return "/aprovacao-embarque" + (partes.isEmpty() ? "" : "?" + String.join("&", partes));
    }

    private void adicionarFiltro(List<String> partes, String nome, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(nome + "=" + URLEncoder.encode(valor, StandardCharsets.UTF_8));
        }
    }
}
