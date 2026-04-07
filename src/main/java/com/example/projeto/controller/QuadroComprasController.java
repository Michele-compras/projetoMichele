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
@RequestMapping("/quadro-compras")
public class QuadroComprasController {

    private static final String[] MARCA_KEYS    = {"MOMI", "ANIME", "AUTHORIA", "YOUCCIE", "BIMBI"};
    private static final String[] MARCA_DISPLAY = {"MOMI", "ANIME", "AUTHORIA", "YOUCCI",  "BIMBI"};
    private static final String[] TIPO_ROWS     = {"Tecido", "Aviamento metro", "Aviamento unidade"};

    private final FichaTecnicaRepository repository;
    private final ColecaoRepository colecaoRepo;
    private final InsumoRepository insumoRepo;

    public QuadroComprasController(FichaTecnicaRepository repository,
                                   ColecaoRepository colecaoRepo,
                                   InsumoRepository insumoRepo) {
        this.repository = repository;
        this.colecaoRepo = colecaoRepo;
        this.insumoRepo = insumoRepo;
    }

    @GetMapping
    public String quadroCompras(@RequestParam(required = false) String colecao,
                                @RequestParam(required = false, defaultValue = "resumo") String aba,
                                Model model) {

        List<FichaTecnica> fichas = repository.findAll();

        List<String> colecoes = colecaoRepo.findAll().stream()
                .map(c -> c.getNome())
                .collect(Collectors.toList());

        List<String> insumos = insumoRepo.findAll().stream()
                .map(i -> i.getNome())
                .collect(Collectors.toList());

        String colecaoAtual = colecao != null ? colecao : (colecoes.isEmpty() ? null : colecoes.get(0));

        // ── Resumo: por coleção → fornecedor × insumo ──
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
                fornInsumoMap.computeIfAbsent(forn, k -> new LinkedHashMap<>())
                             .merge(tipo, f.getQuantidadeComprada(), Double::sum);
            }
            List<Map<String, Object>> fornList = new ArrayList<>();
            fornInsumoMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
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

        // ── Pivot para coleção selecionada: tipoRow -> [momi, anime, authoria, youccie, bimbi] ──
        List<List<Object>> pivotRows = new ArrayList<>();
        double[] colTotals = new double[MARCA_KEYS.length + 1];
        if (colecaoAtual != null) {
            Map<String, double[]> pivot = new LinkedHashMap<>();
            for (String t : TIPO_ROWS) pivot.put(t, new double[MARCA_KEYS.length]);
            for (FichaTecnica f : fichas) {
                if (!colecaoAtual.equals(f.getColecao()) || f.getMarca() == null || f.getQuantidadeComprada() == null) continue;
                String mUpper = f.getMarca().toUpperCase();
                int marcaIdx = -1;
                for (int i = 0; i < MARCA_KEYS.length; i++) {
                    if (mUpper.equals(MARCA_KEYS[i]) || mUpper.startsWith(MARCA_KEYS[i])) { marcaIdx = i; break; }
                }
                if (marcaIdx < 0) continue;
                pivot.get(TIPO_ROWS[tipoIndex(f)])[marcaIdx] += f.getQuantidadeComprada();
            }
            for (String t : TIPO_ROWS) {
                List<Object> row = new ArrayList<>();
                row.add(t);
                double rowTotal = 0;
                double[] vals = pivot.get(t);
                for (int i = 0; i < vals.length; i++) {
                    row.add(vals[i]);
                    rowTotal += vals[i];
                    colTotals[i] += vals[i];
                }
                row.add(rowTotal);
                colTotals[vals.length] += rowTotal;
                pivotRows.add(row);
            }
        }

        model.addAttribute("colecoes",    colecoes);
        model.addAttribute("colecaoAtual", colecaoAtual);
        model.addAttribute("insumos",     insumos);
        model.addAttribute("invernos",    invernos);
        model.addAttribute("veraos",      veraos);
        model.addAttribute("pivotRows",   pivotRows);
        List<Double> colTotalList = new ArrayList<>();
        for (double v : colTotals) colTotalList.add(v);
        model.addAttribute("colTotals",    colTotalList);
        model.addAttribute("marcaDisplay", Arrays.asList(MARCA_DISPLAY));
        model.addAttribute("tipoRows",     Arrays.asList(TIPO_ROWS));
        model.addAttribute("abaAtiva",     aba);

        return "quadro-compras";
    }

    private int tipoIndex(FichaTecnica f) {
        return 0;
    }
}
