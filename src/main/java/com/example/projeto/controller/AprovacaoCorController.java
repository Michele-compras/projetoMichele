package com.example.projeto.controller;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusAmostra;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/aprovacao-cor")
public class AprovacaoCorController {

    private final FichaTecnicaService service;
    private final ColecaoRepository colecaoRepo;
    private final FornecedorRepository fornecedorRepo;

    public AprovacaoCorController(FichaTecnicaService service, ColecaoRepository colecaoRepo,
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
            @RequestParam(required = false) StatusAmostra statusAmostra,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String fornecedor,
            Model model) {

        List<FichaTecnica> fichas;
        boolean temFiltro = colecao != null || tipo != null
                || statusPedido != null || statusAmostra != null
                || dataInicio != null || dataFim != null
                || (codigo != null && !codigo.isBlank())
                || (fornecedor != null && !fornecedor.isBlank());

        if (temFiltro) {
            fichas = service.buscarComFiltros(colecao, tipo, statusPedido, dataInicio, dataFim, null, null, codigo, null, fornecedor);
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
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("statusAmostraList", new StatusAmostra[]{StatusAmostra.PENDENTE, StatusAmostra.EM_ANALISE, StatusAmostra.APROVADO, StatusAmostra.CANCELADO});
        model.addAttribute("colecoesCadastradas", colecaoRepo.findAll());
        model.addAttribute("fornecedoresCadastrados", fornecedorRepo.findAll());
        model.addAttribute("colecaoFiltro", colecao);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("statusPedidoSelecionado", statusPedido);
        model.addAttribute("statusAmostraSelecionado", statusAmostra);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("codigoFiltro", codigo);
        model.addAttribute("fornecedorFiltro", fornecedor);
        model.addAttribute("qtdPorColecao", service.qtdPorColecao());
        model.addAttribute("qtdStatusCorPorColecao", service.qtdStatusCorPorColecao());
        List<Map<String, Object>> leadtime = service.leadtimeAprovacaoCorPorMarca();
        if (colecao != null && !colecao.isBlank()) {
            final String col = colecao.toLowerCase();
            leadtime = leadtime.stream()
                    .filter(r -> r.get("colecao") != null && ((String) r.get("colecao")).toLowerCase().contains(col))
                    .collect(Collectors.toList());
        }
        model.addAttribute("leadtimePorMarca", leadtime);
        model.addAttribute("qtdFornecedorPorColecao", service.qtdFornecedorPorColecao());
        model.addAttribute("totalPorFornecedor", service.totalPorFornecedor());
        return "aprovacao/lista";
    }
}
