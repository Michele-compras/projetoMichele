package com.example.projeto.controller;

import com.example.projeto.model.StatusAmostra;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.model.TipoItem;
import com.example.projeto.repository.FichaTecnicaRepository;
import com.example.projeto.repository.QuadroPlanejamentoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final FichaTecnicaRepository repository;
    private final QuadroPlanejamentoRepository quadroRepository;

    public DashboardController(FichaTecnicaRepository repository, QuadroPlanejamentoRepository quadroRepository) {
        this.repository = repository;
        this.quadroRepository = quadroRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long total = repository.count();

        // Totais gerais
        long qtdTecido    = repository.countByTipo(TipoItem.TECIDO);
        long qtdAcessorio = repository.countByTipo(TipoItem.ACESSORIO);
        long qtdEmAndamento = repository.countByStatusPedido(StatusPedido.EM_ANDAMENTO);
        long qtdEmbarcado   = repository.countByStatusPedido(StatusPedido.EMBARCADO);
        long qtdRecebido    = repository.countByStatusPedido(StatusPedido.RECEBIDO);
        long qtdCancelado   = repository.countByStatusPedido(StatusPedido.CANCELADO);
        long corPendente  = repository.countByStatusAmostraCor(StatusAmostra.PENDENTE);
        long corAprovado  = repository.countByStatusAmostraCor(StatusAmostra.APROVADO);
        long corReprovado = repository.countByStatusAmostraCor(StatusAmostra.REPROVADO);
        long prodPendente  = repository.countByStatusAmostraProducao(StatusAmostra.PENDENTE);
        long prodAprovado  = repository.countByStatusAmostraProducao(StatusAmostra.APROVADO);
        long prodReprovado = repository.countByStatusAmostraProducao(StatusAmostra.REPROVADO);

        model.addAttribute("total", total);
        model.addAttribute("qtdTecido", qtdTecido);
        model.addAttribute("qtdAcessorio", qtdAcessorio);
        model.addAttribute("qtdEmAndamento", qtdEmAndamento);
        model.addAttribute("qtdEmbarcado", qtdEmbarcado);
        model.addAttribute("qtdRecebido", qtdRecebido);
        model.addAttribute("qtdCancelado", qtdCancelado);
        model.addAttribute("corPendente", corPendente);
        model.addAttribute("corAprovado", corAprovado);
        model.addAttribute("corReprovado", corReprovado);
        model.addAttribute("prodPendente", prodPendente);
        model.addAttribute("prodAprovado", prodAprovado);
        model.addAttribute("prodReprovado", prodReprovado);

        // Por marca
        List<Object[]> marcaData = repository.countByMarca();
        List<String> marcaLabels = new ArrayList<>();
        List<Long> marcaValues = new ArrayList<>();
        for (Object[] row : marcaData) {
            marcaLabels.add((String) row[0]);
            marcaValues.add((Long) row[1]);
        }
        model.addAttribute("marcaLabels", marcaLabels);
        model.addAttribute("marcaValues", marcaValues);

        // ── Por coleção: totais ──────────────────────────────────────
        List<Object[]> colecaoData = repository.countByColecao();
        List<String> colecaoLabels = new ArrayList<>();
        List<Long>   colecaoValues = new ArrayList<>();
        for (Object[] row : colecaoData) {
            colecaoLabels.add((String) row[0]);
            colecaoValues.add((Long) row[1]);
        }
        model.addAttribute("colecaoLabels", colecaoLabels);
        model.addAttribute("colecaoValues", colecaoValues);

        // ── Por coleção: tipo ────────────────────────────────────────
        Map<String, Long> tecidoPorColecao    = initMap(colecaoLabels);
        Map<String, Long> acessorioPorColecao = initMap(colecaoLabels);
        for (Object[] row : repository.countByColecaoAndTipo()) {
            String col = (String) row[0];
            TipoItem tipo = (TipoItem) row[1];
            Long cnt = (Long) row[2];
            if (tipo == TipoItem.TECIDO) tecidoPorColecao.put(col, cnt);
            else acessorioPorColecao.put(col, cnt);
        }
        model.addAttribute("colecaoTecidoValues",    new ArrayList<>(tecidoPorColecao.values()));
        model.addAttribute("colecaoAcessorioValues", new ArrayList<>(acessorioPorColecao.values()));

        // ── Por coleção: status pedido ───────────────────────────────
        Map<String, Long> pedidoEmAndamento = initMap(colecaoLabels);
        Map<String, Long> pedidoEmbarcado   = initMap(colecaoLabels);
        Map<String, Long> pedidoRecebido    = initMap(colecaoLabels);
        Map<String, Long> pedidoCancelado   = initMap(colecaoLabels);
        for (Object[] row : repository.countByColecaoAndStatusPedido()) {
            String col = (String) row[0];
            StatusPedido st = (StatusPedido) row[1];
            Long cnt = (Long) row[2];
            if (st == StatusPedido.EM_ANDAMENTO) pedidoEmAndamento.put(col, cnt);
            else if (st == StatusPedido.EMBARCADO)   pedidoEmbarcado.put(col, cnt);
            else if (st == StatusPedido.RECEBIDO)    pedidoRecebido.put(col, cnt);
            else if (st == StatusPedido.CANCELADO)   pedidoCancelado.put(col, cnt);
        }
        model.addAttribute("colecaoPedidoEmAndamento", new ArrayList<>(pedidoEmAndamento.values()));
        model.addAttribute("colecaoPedidoEmbarcado",   new ArrayList<>(pedidoEmbarcado.values()));
        model.addAttribute("colecaoPedidoRecebido",    new ArrayList<>(pedidoRecebido.values()));
        model.addAttribute("colecaoPedidoCancelado",   new ArrayList<>(pedidoCancelado.values()));

        // ── Por coleção: amostra cor ─────────────────────────────────
        Map<String, Long> corAprovadoMap  = initMap(colecaoLabels);
        Map<String, Long> corReprovadoMap = initMap(colecaoLabels);
        Map<String, Long> corPendenteMap  = initMap(colecaoLabels);
        for (Object[] row : repository.countByColecaoAndStatusAmostraCor()) {
            String col = (String) row[0];
            StatusAmostra st = (StatusAmostra) row[1];
            Long cnt = (Long) row[2];
            if (st == StatusAmostra.APROVADO)       corAprovadoMap.put(col, cnt);
            else if (st == StatusAmostra.REPROVADO) corReprovadoMap.put(col, cnt);
            else                                    corPendenteMap.put(col, cnt);
        }
        model.addAttribute("colecaoCorAprovado",  new ArrayList<>(corAprovadoMap.values()));
        model.addAttribute("colecaoCorReprovado", new ArrayList<>(corReprovadoMap.values()));
        model.addAttribute("colecaoCorPendente",  new ArrayList<>(corPendenteMap.values()));

        // ── Por coleção: amostra embarque ────────────────────────────
        Map<String, Long> prodAprovadoMap  = initMap(colecaoLabels);
        Map<String, Long> prodReprovadoMap = initMap(colecaoLabels);
        Map<String, Long> prodPendenteMap  = initMap(colecaoLabels);
        for (Object[] row : repository.countByColecaoAndStatusAmostraProducao()) {
            String col = (String) row[0];
            StatusAmostra st = (StatusAmostra) row[1];
            Long cnt = (Long) row[2];
            if (st == StatusAmostra.APROVADO)       prodAprovadoMap.put(col, cnt);
            else if (st == StatusAmostra.REPROVADO) prodReprovadoMap.put(col, cnt);
            else                                    prodPendenteMap.put(col, cnt);
        }
        model.addAttribute("colecaoProdAprovado",  new ArrayList<>(prodAprovadoMap.values()));
        model.addAttribute("colecaoProdReprovado", new ArrayList<>(prodReprovadoMap.values()));
        model.addAttribute("colecaoProdPendente",  new ArrayList<>(prodPendenteMap.values()));

        // ── Por coleção: cotado vs aprovado do quadro de planejamento ─────
        List<Object[]> qtdColecaoTipo = quadroRepository.sumCotadoAprovadoByColecao();
        long qtdTotalOrc = 0, qtdTotalCmp = 0;
        for (Object[] row : qtdColecaoTipo) {
            qtdTotalOrc += row[1] != null ? ((Number) row[1]).longValue() : 0;
            qtdTotalCmp += row[2] != null ? ((Number) row[2]).longValue() : 0;
        }
        model.addAttribute("qtdColecaoTipo", qtdColecaoTipo);
        model.addAttribute("qtdTotalOrc", qtdTotalOrc);
        model.addAttribute("qtdTotalCmp", qtdTotalCmp);


        return "dashboard";
    }

    private Map<String, Long> initMap(List<String> keys) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (String k : keys) map.put(k, 0L);
        return map;
    }
}
