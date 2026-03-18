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

@Controller
@RequestMapping("/aprovacao-cor")
public class AprovacaoCorController {

    private final FichaTecnicaService service;

    public AprovacaoCorController(FichaTecnicaService service) {
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
                        .filter(f -> filtroStatus.equals(f.getStatusAmostraCor()))
                        .toList();
            }
        } else {
            fichas = service.listarTodas();
        }

        model.addAttribute("fichas", fichas);
        model.addAttribute("tipos", TipoItem.values());
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("statusAmostraList", new StatusAmostra[]{StatusAmostra.PENDENTE, StatusAmostra.APROVADO});
        model.addAttribute("colecaoFiltro", colecao);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("statusPedidoSelecionado", statusPedido);
        model.addAttribute("statusAmostraSelecionado", statusAmostra);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("qtdPorColecao", service.qtdPorColecao());
        model.addAttribute("qtdStatusCorPorColecao", service.qtdStatusCorPorColecao());
        model.addAttribute("leadtimePorMarca", service.leadtimeAprovacaoCorPorMarca());
        model.addAttribute("qtdFornecedorPorColecao", service.qtdFornecedorPorColecao());
        model.addAttribute("totalPorFornecedor", service.totalPorFornecedor());
        return "aprovacao/lista";
    }
}
