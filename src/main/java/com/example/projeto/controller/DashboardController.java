package com.example.projeto.controller;

import com.example.projeto.model.StatusAmostra;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.model.TipoItem;
import com.example.projeto.repository.FichaTecnicaRepository;
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

    public DashboardController(FichaTecnicaRepository repository) {
        this.repository = repository;
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

        // ── Por coleção: quantidades orçadas e compradas por tipo ─────
        Map<String, Double> colOrcTecido = initDoubleMap(colecaoLabels);
        Map<String, Double> colCmpTecido = initDoubleMap(colecaoLabels);
        Map<String, Double> colOrcAcess  = initDoubleMap(colecaoLabels);
        Map<String, Double> colCmpAcess  = initDoubleMap(colecaoLabels);
        for (Object[] row : repository.sumQuantidadeByColecaoAndTipo()) {
            String col  = (String) row[0];
            TipoItem tp = (TipoItem) row[1];
            double orc  = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            double cmp  = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            if (tp == TipoItem.TECIDO) { colOrcTecido.put(col, orc); colCmpTecido.put(col, cmp); }
            else                       { colOrcAcess.put(col, orc);  colCmpAcess.put(col, cmp); }
        }
        model.addAttribute("colOrcTecido", new ArrayList<>(colOrcTecido.values()));
        model.addAttribute("colCmpTecido", new ArrayList<>(colCmpTecido.values()));
        model.addAttribute("colOrcAcess",  new ArrayList<>(colOrcAcess.values()));
        model.addAttribute("colCmpAcess",  new ArrayList<>(colCmpAcess.values()));

        // ── Por marca: quantidades orçadas e compradas por tipo ───────
        List<String> marcaQtdLabels = new ArrayList<>();
        Map<String, Double> mOrcTecido = new LinkedHashMap<>();
        Map<String, Double> mCmpTecido = new LinkedHashMap<>();
        Map<String, Double> mOrcAcess  = new LinkedHashMap<>();
        Map<String, Double> mCmpAcess  = new LinkedHashMap<>();
        for (Object[] row : repository.sumQuantidadeByMarcaAndTipo()) {
            String marc = (String) row[0];
            TipoItem tp = (TipoItem) row[1];
            double orc  = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            double cmp  = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            if (!mOrcTecido.containsKey(marc)) {
                marcaQtdLabels.add(marc);
                mOrcTecido.put(marc, 0.0); mCmpTecido.put(marc, 0.0);
                mOrcAcess.put(marc, 0.0);  mCmpAcess.put(marc, 0.0);
            }
            if (tp == TipoItem.TECIDO) { mOrcTecido.put(marc, orc); mCmpTecido.put(marc, cmp); }
            else                       { mOrcAcess.put(marc, orc);  mCmpAcess.put(marc, cmp); }
        }
        model.addAttribute("marcaQtdLabels", marcaQtdLabels);
        model.addAttribute("mOrcTecido", new ArrayList<>(mOrcTecido.values()));
        model.addAttribute("mCmpTecido", new ArrayList<>(mCmpTecido.values()));
        model.addAttribute("mOrcAcess",  new ArrayList<>(mOrcAcess.values()));
        model.addAttribute("mCmpAcess",  new ArrayList<>(mCmpAcess.values()));

        return "dashboard";
    }

    private Map<String, Long> initMap(List<String> keys) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (String k : keys) map.put(k, 0L);
        return map;
    }

    private Map<String, Double> initDoubleMap(List<String> keys) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (String k : keys) map.put(k, 0.0);
        return map;
    }
}
