package com.example.projeto.controller;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.TipoItem;
import com.example.projeto.repository.FichaTecnicaRepository;
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

    public CompradoFornecedorController(FichaTecnicaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String compradoFornecedor(@RequestParam(required = false) String colecao,
                                     @RequestParam(required = false, defaultValue = "resumo") String aba,
                                     Model model) {

        List<FichaTecnica> fichas = repository.findAll();

        List<String> colecoes = fichas.stream()
                .map(FichaTecnica::getColecao)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        String colecaoAtual = colecao != null ? colecao : (colecoes.isEmpty() ? null : colecoes.get(0));

        // ── Resumo: cards por coleção com totais por tipo ──
        List<Map<String, Object>> invernos = new ArrayList<>();
        List<Map<String, Object>> veraos   = new ArrayList<>();
        for (String col : colecoes) {
            double tecido = 0, avioMetro = 0, avioUnid = 0;
            for (FichaTecnica f : fichas) {
                if (!col.equals(f.getColecao()) || f.getQuantidadeComprada() == null) continue;
                int idx = tipoIndex(f);
                if (idx == 0)      tecido    += f.getQuantidadeComprada();
                else if (idx == 1) avioMetro += f.getQuantidadeComprada();
                else               avioUnid  += f.getQuantidadeComprada();
            }
            // Total por fornecedor nesta coleção
            Map<String, Double> fornMap = new LinkedHashMap<>();
            for (FichaTecnica f : fichas) {
                if (!col.equals(f.getColecao()) || f.getQuantidadeComprada() == null
                        || f.getFornecedor() == null || f.getFornecedor().isBlank()) continue;
                fornMap.merge(f.getFornecedor().trim(), f.getQuantidadeComprada().doubleValue(), Double::sum);
            }
            List<Map<String, Object>> fornList = new ArrayList<>();
            fornMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach(e -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("nome", e.getKey());
                        m.put("qty", e.getValue());
                        fornList.add(m);
                    });

            Map<String, Object> card = new LinkedHashMap<>();
            card.put("colecao",     col);
            card.put("tecido",      tecido);
            card.put("avioMetro",   avioMetro);
            card.put("avioUnid",    avioUnid);
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
        model.addAttribute("invernos",     invernos);
        model.addAttribute("veraos",       veraos);
        model.addAttribute("fornecedores", fornecedores);
        model.addAttribute("pivotRows",    pivotRows);
        model.addAttribute("colTotals",    colTotals);
        model.addAttribute("abaAtiva",     aba);

        return "comprado-fornecedor";
    }

    private int tipoIndex(FichaTecnica f) {
        if (f.getTipo() == TipoItem.TECIDO) return 0;
        if (f.getTipo() == TipoItem.ACESSORIO_METRO) return 1;
        return 2;
    }
}
