package com.example.projeto.controller;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.repository.ColecaoRepository;
import com.example.projeto.repository.FichaTecnicaRepository;
import com.example.projeto.repository.InsumoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/comprado-fornecedor")
public class CompradoFornecedorController {

    private static final String[] TIPO_ROWS = {
        "Tecido", "Aviamento em Metro", "Aviamento em Unidade"
    };

    private final FichaTecnicaRepository repository;
    private final InsumoRepository insumoRepo;
    private final ColecaoRepository colecaoRepo;

    public CompradoFornecedorController(FichaTecnicaRepository repository, InsumoRepository insumoRepo,
                                        ColecaoRepository colecaoRepo) {
        this.repository = repository;
        this.insumoRepo = insumoRepo;
        this.colecaoRepo = colecaoRepo;
    }

    @GetMapping
    public String compradoFornecedor(@RequestParam(required = false) String colecao,
                                     @RequestParam(required = false, defaultValue = "resumo") String aba,
                                     Model model) {

        List<FichaTecnica> fichas = repository.findAll();

        List<String> colecoes = colecaoRepo.findAll().stream()
                .map(c -> c.getNome())
                .collect(Collectors.toList());

        String colecaoAtual = colecao != null ? colecao : (colecoes.isEmpty() ? null : colecoes.get(0));

        // Lista de insumos do cadastro prévio (usada como colunas no resumo)
        List<String> insumos = insumoRepo.findAll().stream()
                .map(i -> i.getNome())
                .collect(Collectors.toList());

        // ── Resumo: cards por coleção com totais por fornecedor × insumo ──
        List<Map<String, Object>> invernos = new ArrayList<>();
        List<Map<String, Object>> veraos   = new ArrayList<>();
        for (String col : colecoes) {
            // fornecedor -> insumo -> qty
            Map<String, Map<String, Double>> fornInsumoMap = new LinkedHashMap<>();
            for (FichaTecnica f : fichas) {
                if (!col.equals(f.getColecao()) || f.getQuantidadeComprada() == null
                        || f.getFornecedor() == null || f.getFornecedor().isBlank()
                        || f.getTipo() == null) continue;
                String forn = f.getFornecedor().trim();
                String tipo = f.getTipo();
                fornInsumoMap
                    .computeIfAbsent(forn, k -> new LinkedHashMap<>())
                    .merge(tipo, f.getQuantidadeComprada(), Double::sum);
            }
            // Montar lista de linhas: cada linha tem nome + qty por insumo + total
            List<Map<String, Object>> fornList = new ArrayList<>();
            fornInsumoMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("nome", e.getKey());
                        double total = 0;
                        for (String ins : insumos) {
                            double qty = e.getValue().getOrDefault(ins, 0.0);
                            m.put(ins, qty);
                            total += qty;
                        }
                        m.put("total", total);
                        fornList.add(m);
                    });

            Map<String, Object> card = new LinkedHashMap<>();
            card.put("colecao",      col);
            card.put("fornecedores", fornList);
            if (col.toLowerCase().contains("inverno")) invernos.add(card);
            else                                       veraos.add(card);
        }

        // ── Pivot para coleção selecionada: tipo × fornecedor ──
        List<String> fornecedores = new ArrayList<>();
        List<List<Object>> pivotRows = new ArrayList<>();
        List<Double> colTotals = new ArrayList<>();

        if (colecaoAtual != null) {
            // Descobrir fornecedores da coleção
            fornecedores = fichas.stream()
                    .filter(f -> colecaoAtual.equals(f.getColecao())
                            && f.getFornecedor() != null
                            && !f.getFornecedor().isBlank()
                            && f.getQuantidadeComprada() != null)
                    .map(f -> f.getFornecedor().trim())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Montar pivot: tipo -> [fornecedor0, fornecedor1, ...]
            Map<String, double[]> pivot = new LinkedHashMap<>();
            for (String t : TIPO_ROWS) pivot.put(t, new double[fornecedores.size()]);

            for (FichaTecnica f : fichas) {
                if (!colecaoAtual.equals(f.getColecao())
                        || f.getFornecedor() == null
                        || f.getQuantidadeComprada() == null) continue;
                String forn = f.getFornecedor().trim();
                int fornIdx = fornecedores.indexOf(forn);
                if (fornIdx < 0) continue;
                pivot.get(TIPO_ROWS[tipoIndex(f)])[fornIdx] += f.getQuantidadeComprada();
            }

            // Inicializar totais de coluna
            double[] colTotalsArr = new double[fornecedores.size() + 1];

            for (String t : TIPO_ROWS) {
                List<Object> row = new ArrayList<>();
                row.add(t);
                double rowTotal = 0;
                double[] vals = pivot.get(t);
                for (int i = 0; i < vals.length; i++) {
                    row.add(vals[i]);
                    rowTotal += vals[i];
                    colTotalsArr[i] += vals[i];
                }
                row.add(rowTotal);
                colTotalsArr[vals.length] += rowTotal;
                pivotRows.add(row);
            }

            for (double v : colTotalsArr) colTotals.add(v);
        }

        model.addAttribute("colecoes",     colecoes);
        model.addAttribute("colecaoAtual", colecaoAtual);
        model.addAttribute("insumos",      insumos);
        model.addAttribute("invernos",     invernos);
        model.addAttribute("veraos",       veraos);
        model.addAttribute("fornecedores", fornecedores);
        model.addAttribute("pivotRows",    pivotRows);
        model.addAttribute("colTotals",    colTotals);
        model.addAttribute("abaAtiva",     aba);

        return "comprado-fornecedor";
    }

    private int tipoIndex(FichaTecnica f) {
        return 0;
    }
}
