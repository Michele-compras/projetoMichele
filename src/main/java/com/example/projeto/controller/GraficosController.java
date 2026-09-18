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

        List<String> cotAprLabels    = new ArrayList<>();
        List<Integer> cotAprCotado    = new ArrayList<>();
        List<Integer> cotAprAprovado  = new ArrayList<>();
        List<Integer> cotAprCancelado = new ArrayList<>();
        int totalCotado = 0, totalAprovado = 0, totalCancelado = 0;

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
            cotAprLabels.add(col);
            cotAprCotado.add(cotado);
            cotAprAprovado.add(aprovado);
            cotAprCancelado.add(cancelado);
            totalCotado    += cotado;
            totalAprovado  += aprovado;
            totalCancelado += cancelado;
        }

        model.addAttribute("cotAprLabels", cotAprLabels);
        model.addAttribute("cotAprCotado", cotAprCotado);
        model.addAttribute("cotAprAprovado", cotAprAprovado);
        model.addAttribute("cotAprCancelado", cotAprCancelado);
        model.addAttribute("cotAprTotalCotado", totalCotado);
        model.addAttribute("cotAprTotalAprovado", totalAprovado);
        model.addAttribute("cotAprTotalCancelado", totalCancelado);

        return "graficos";
    }

    private int soma(Integer... valores) {
        int total = 0;
        for (Integer v : valores) total += v != null ? v : 0;
        return total;
    }
}
