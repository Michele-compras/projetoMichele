package com.example.projeto.controller;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusAmostra;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.model.TipoItem;
import com.example.projeto.service.FichaTecnicaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/aprovacao-embarque")
public class AprovacaoEmbarqueController {

    private final FichaTecnicaService service;

    public AprovacaoEmbarqueController(FichaTecnicaService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String colecao,
            @RequestParam(required = false) TipoItem tipo,
            @RequestParam(required = false) StatusPedido statusPedido,
            @RequestParam(required = false) StatusAmostra statusAmostra,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            Model model) {

        List<FichaTecnica> fichas;
        boolean temFiltro = colecao != null || tipo != null
                || statusPedido != null || statusAmostra != null
                || dataInicio != null || dataFim != null;

        if (temFiltro) {
            fichas = service.buscarComFiltros(colecao, tipo, statusPedido, dataInicio, dataFim);
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
        model.addAttribute("tipos", TipoItem.values());
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("statusAmostraList", StatusAmostra.values());
        model.addAttribute("colecaoFiltro", colecao);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("statusPedidoSelecionado", statusPedido);
        model.addAttribute("statusAmostraSelecionado", statusAmostra);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
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
        return "aprovacao/embarque";
    }
}
