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
import java.util.List;

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
        String busca = colecaoFiltro != null ? colecaoFiltro.trim().toLowerCase() : "";
        model.addAttribute("colecaoFiltro", colecaoFiltro != null ? colecaoFiltro : "");

        // ── Gráfico 1: Cotado x Aprovado por Coleção ───────────────────────────
        // Mesma regra da aba Resumo de /quadro-planejamento: parte da lista mestre
        // de coleções (assim uma coleção ainda sem cotação aparece zerada) e ignora
        // linhas cujo insumo saiu do Cadastro Prévio. Se aquela regra mudar, esta
        // precisa mudar junto.
        List<String> insumos = insumoRepo.findAll().stream().map(i -> i.getNome()).toList();

        List<String> cotAprLabels   = new ArrayList<>();
        List<Integer> cotAprCotado   = new ArrayList<>();
        List<Integer> cotAprAprovado = new ArrayList<>();
        int totalCotado = 0, totalAprovado = 0;

        for (var colecao : colecaoRepo.findAll()) {
            String col = colecao.getNome();
            if (col == null) continue;
            if (!busca.isBlank() && !col.toLowerCase().contains(busca)) continue;

            int cotado = 0, aprovado = 0;
            for (QuadroPlanejamento q : quadroRepo.findByColecaoOrderByTipoSolicitacaoAsc(col)) {
                if (!insumos.contains(q.getTipoSolicitacao())) continue;
                cotado   += soma(q.getAnimeCotado(), q.getMomiCotado(), q.getAuthoriaCotado(),
                                 q.getBimbiCotado(), q.getYoucciecotado());
                aprovado += soma(q.getAnimeAprovado(), q.getMomiAprovado(), q.getAuthoriaAprovado(),
                                 q.getBimbiAprovado(), q.getYouccieeAprovado());
            }
            cotAprLabels.add(col);
            cotAprCotado.add(cotado);
            cotAprAprovado.add(aprovado);
            totalCotado   += cotado;
            totalAprovado += aprovado;
        }

        model.addAttribute("cotAprLabels", cotAprLabels);
        model.addAttribute("cotAprCotado", cotAprCotado);
        model.addAttribute("cotAprAprovado", cotAprAprovado);
        model.addAttribute("cotAprTotalCotado", totalCotado);
        model.addAttribute("cotAprTotalAprovado", totalAprovado);

        return "graficos";
    }

    private int soma(Integer... valores) {
        int total = 0;
        for (Integer v : valores) total += v != null ? v : 0;
        return total;
    }
}
