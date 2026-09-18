package com.example.projeto.controller;

import com.example.projeto.model.QuadroPlanejamento;
import com.example.projeto.repository.ColecaoRepository;
import com.example.projeto.repository.InsumoRepository;
import com.example.projeto.repository.QuadroPlanejamentoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tela dedicada aos gráficos. Cada gráfico novo entra aqui: monta-se a série
 * neste controller e desenha-se o card correspondente em templates/graficos.html.
 */
@Controller
@RequestMapping("/graficos")
public class GraficosController {

    private final QuadroPlanejamentoRepository quadroRepo;
    private final ColecaoRepository colecaoRepo;
    private final InsumoRepository insumoRepo;

    public GraficosController(QuadroPlanejamentoRepository quadroRepo,
                              ColecaoRepository colecaoRepo,
                              InsumoRepository insumoRepo) {
        this.quadroRepo = quadroRepo;
        this.colecaoRepo = colecaoRepo;
        this.insumoRepo = insumoRepo;
    }

    @GetMapping
    public String exibir(@RequestParam(required = false) String colecaoFiltro, Model model) {
        // O filtro é uma lista das coleções cadastradas no Cadastro Prévio (tabela
        // Colecao), e não texto livre: assim só se filtra por coleção que existe.
        var colecoesCadastradas = colecaoRepo.findAll();
        model.addAttribute("colecoesCadastradas", colecoesCadastradas);

        String selecionada = colecaoFiltro != null ? colecaoFiltro.trim() : "";
        model.addAttribute("colecaoFiltro", selecionada);

        // ── Gráfico 1: Cotado x Aprovado por Coleção ───────────────────────────
        // Mesma regra da aba Resumo de /quadro-planejamento: parte da lista mestre
        // de coleções (assim uma coleção ainda sem cotação aparece zerada) e ignora
        // linhas cujo insumo saiu do Cadastro Prévio. Se aquela regra mudar, esta
        // precisa mudar junto.
        List<String> insumos = insumoRepo.findAll().stream().map(i -> i.getNome()).toList();

        // Verão e Inverno saem em séries separadas para virarem duas linhas na tela:
        // Verão em cima, Inverno embaixo. Mesma classificação pelo nome que a aba
        // Resumo de /quadro-planejamento usa.
        List<String> veraoLabels     = new ArrayList<>();
        List<Integer> veraoCotado     = new ArrayList<>();
        List<Integer> veraoAprovado   = new ArrayList<>();
        List<Integer> veraoCancelado  = new ArrayList<>();
        int veraoTotCotado = 0, veraoTotAprovado = 0, veraoTotCancelado = 0;

        List<String> invernoLabels    = new ArrayList<>();
        List<Integer> invernoCotado    = new ArrayList<>();
        List<Integer> invernoAprovado  = new ArrayList<>();
        List<Integer> invernoCancelado = new ArrayList<>();
        int invernoTotCotado = 0, invernoTotAprovado = 0, invernoTotCancelado = 0;

        for (var colecao : colecoesCadastradas) {
            String col = colecao.getNome();
            if (col == null) continue;
            // Comparação exata: o valor vem da própria lista, não digitado à mão.
            if (!selecionada.isBlank() && !selecionada.equals(col)) continue;

            int cotado = 0, aprovado = 0, cancelado = 0;
            for (QuadroPlanejamento q : quadroRepo.findByColecaoOrderByTipoSolicitacaoAsc(col)) {
                if (!insumos.contains(q.getTipoSolicitacao())) continue;
                cotado    += soma(q.getAnimeCotado(), q.getMomiCotado(), q.getAuthoriaCotado(),
                                  q.getBimbiCotado(), q.getYoucciecotado());
                aprovado  += soma(q.getAnimeAprovado(), q.getMomiAprovado(), q.getAuthoriaAprovado(),
                                  q.getBimbiAprovado(), q.getYouccieeAprovado());
                cancelado += soma(q.getAnimeCancelado(), q.getMomiCancelado(), q.getAuthoriaCancelado(),
                                  q.getBimbiCancelado(), q.getYoucciecancelado());
            }

            if (col.toLowerCase().contains("inverno")) {
                invernoLabels.add(col);
                invernoCotado.add(cotado);
                invernoAprovado.add(aprovado);
                invernoCancelado.add(cancelado);
                invernoTotCotado    += cotado;
                invernoTotAprovado  += aprovado;
                invernoTotCancelado += cancelado;
            } else {
                veraoLabels.add(col);
                veraoCotado.add(cotado);
                veraoAprovado.add(aprovado);
                veraoCancelado.add(cancelado);
                veraoTotCotado    += cotado;
                veraoTotAprovado  += aprovado;
                veraoTotCancelado += cancelado;
            }
        }

        // Uma linha por estação, nesta ordem: Verão em cima, Inverno embaixo.
        // O template percorre esta lista, então não há markup duplicado.
        List<Map<String, Object>> blocos = new ArrayList<>();
        blocos.add(bloco("Verão", veraoLabels, veraoCotado, veraoAprovado, veraoCancelado,
                         veraoTotCotado, veraoTotAprovado, veraoTotCancelado));
        blocos.add(bloco("Inverno", invernoLabels, invernoCotado, invernoAprovado, invernoCancelado,
                         invernoTotCotado, invernoTotAprovado, invernoTotCancelado));
        model.addAttribute("blocos", blocos);

        return "graficos";
    }

    /** Monta um bloco do gráfico (uma estação = uma linha na tela). */
    private Map<String, Object> bloco(String titulo, List<String> labels,
                                      List<Integer> cotado, List<Integer> aprovado, List<Integer> cancelado,
                                      int totCotado, int totAprovado, int totCancelado) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("titulo", titulo);
        b.put("labels", labels);
        b.put("cotado", cotado);
        b.put("aprovado", aprovado);
        b.put("cancelado", cancelado);
        b.put("totCotado", totCotado);
        b.put("totAprovado", totAprovado);
        b.put("totCancelado", totCancelado);
        return b;
    }

    private int soma(Integer... valores) {
        int total = 0;
        for (Integer v : valores) total += v != null ? v : 0;
        return total;
    }
}
