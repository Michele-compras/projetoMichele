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
@RequestMapping("/quadro-compras")
public class QuadroComprasController {

    private static final String[] MARCA_KEYS    = {"MOMI", "ANIME", "AUTHORIA", "YOUCCIE", "BIMBI"};
    private static final String[] MARCA_DISPLAY = {"MOMI", "ANIME", "AUTHORIA", "YOUCCI",  "BIMBI"};
    private static final String[] TIPO_ROWS     = {"Tecido", "Aviamento metro", "Aviamento unidade"};

    private final FichaTecnicaRepository repository;

    public QuadroComprasController(FichaTecnicaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String quadroCompras(@RequestParam(required = false) String colecao,
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

        // ── Resumo: listas pre-filtradas por Inverno/Verão ──
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
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("colecao",   col);
            card.put("tecido",    tecido);
            card.put("avioMetro", avioMetro);
            card.put("avioUnid",  avioUnid);
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

        model.addAttribute("colecoes", colecoes);
        model.addAttribute("colecaoAtual", colecaoAtual);
        model.addAttribute("invernos", invernos);
        model.addAttribute("veraos",   veraos);
        model.addAttribute("pivotRows", pivotRows);
        List<Double> colTotalList = new ArrayList<>();
        for (double v : colTotals) colTotalList.add(v);
        model.addAttribute("colTotals", colTotalList);
        model.addAttribute("marcaDisplay", Arrays.asList(MARCA_DISPLAY));
        model.addAttribute("tipoRows", Arrays.asList(TIPO_ROWS));
        model.addAttribute("abaAtiva", aba);

        return "quadro-compras";
    }

    private int tipoIndex(FichaTecnica f) {
        if (f.getTipo() == TipoItem.TECIDO) return 0;
        if (f.getLargura() != null && !f.getLargura().isBlank()) return 1;
        return 2;
    }
}
