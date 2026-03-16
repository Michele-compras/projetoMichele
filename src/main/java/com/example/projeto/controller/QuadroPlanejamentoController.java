package com.example.projeto.controller;

import com.example.projeto.model.QuadroPlanejamento;
import com.example.projeto.repository.QuadroPlanejamentoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/quadro-planejamento")
public class QuadroPlanejamentoController {

    private static final List<String> TIPOS = Arrays.asList(
        "Tecido",
        "Aviamento em Metro",
        "Aviamento em Unidade"
    );

    private final QuadroPlanejamentoRepository repository;

    public QuadroPlanejamentoController(QuadroPlanejamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String exibir(@RequestParam(required = false) String colecao,
                         @RequestParam(required = false, defaultValue = "quadro") String aba,
                         Model model) {
        List<String> colecoes = repository.findDistinctColecoes();

        if (colecao == null || colecao.isBlank()) {
            colecao = colecoes.isEmpty() ? "" : colecoes.get(0);
        }

        final String colecaoFinal = colecao;
        List<QuadroPlanejamento> linhas = new ArrayList<>();
        for (String tipo : TIPOS) {
            QuadroPlanejamento linha = repository
                .findByColecaoAndTipoSolicitacao(colecaoFinal, tipo)
                .orElseGet(() -> {
                    QuadroPlanejamento q = new QuadroPlanejamento();
                    q.setColecao(colecaoFinal);
                    q.setTipoSolicitacao(tipo);
                    return q;
                });
            linhas.add(linha);
        }

        // Dados do resumo
        List<java.util.Map<String, Object>> invernos = new ArrayList<>();
        List<java.util.Map<String, Object>> veraos   = new ArrayList<>();
        for (String col : colecoes) {
            List<QuadroPlanejamento> ls = repository.findByColecaoOrderByTipoSolicitacaoAsc(col);
            int cotado = 0, aprovado = 0, cancelado = 0;
            for (QuadroPlanejamento q : ls) {
                cotado    += soma(q.getAnimeCotado(), q.getMomiCotado(), q.getAuthoriaCotado(), q.getBimbiCotado(), q.getYoucciecotado());
                aprovado  += soma(q.getAnimeAprovado(), q.getMomiAprovado(), q.getAuthoriaAprovado(), q.getBimbiAprovado(), q.getYouccieeAprovado());
                cancelado += soma(q.getAnimeCancelado(), q.getMomiCancelado(), q.getAuthoriaCancelado(), q.getBimbiCancelado(), q.getYoucciecancelado());
            }
            java.util.Map<String, Object> card = new java.util.LinkedHashMap<>();
            card.put("colecao", col);
            card.put("cotado", cotado);
            card.put("aprovado", aprovado);
            card.put("cancelado", cancelado);
            if (col.toLowerCase().contains("inverno")) invernos.add(card);
            else veraos.add(card);
        }

        model.addAttribute("linhas", linhas);
        model.addAttribute("colecaoAtual", colecaoFinal);
        model.addAttribute("colecoes", colecoes);
        model.addAttribute("invernos", invernos);
        model.addAttribute("veraos", veraos);
        model.addAttribute("abaAtiva", aba);
        return "quadro-planejamento";
    }

    @GetMapping("/resumo")
    public String resumo(Model model) {
        List<String> colecoes = repository.findDistinctColecoes();

        List<java.util.Map<String, Object>> invernos = new ArrayList<>();
        List<java.util.Map<String, Object>> veraos   = new ArrayList<>();

        for (String col : colecoes) {
            List<QuadroPlanejamento> linhas = repository.findByColecaoOrderByTipoSolicitacaoAsc(col);
            int cotado = 0, aprovado = 0, cancelado = 0;
            for (QuadroPlanejamento q : linhas) {
                cotado    += soma(q.getAnimeCotado(), q.getMomiCotado(), q.getAuthoriaCotado(), q.getBimbiCotado(), q.getYoucciecotado());
                aprovado  += soma(q.getAnimeAprovado(), q.getMomiAprovado(), q.getAuthoriaAprovado(), q.getBimbiAprovado(), q.getYouccieeAprovado());
                cancelado += soma(q.getAnimeCancelado(), q.getMomiCancelado(), q.getAuthoriaCancelado(), q.getBimbiCancelado(), q.getYoucciecancelado());
            }
            java.util.Map<String, Object> card = new java.util.LinkedHashMap<>();
            card.put("colecao",   col);
            card.put("cotado",    cotado);
            card.put("aprovado",  aprovado);
            card.put("cancelado", cancelado);

            String lower = col.toLowerCase();
            if (lower.contains("inverno"))      invernos.add(card);
            else if (lower.contains("ver"))     veraos.add(card);
            else                                veraos.add(card);
        }

        model.addAttribute("invernos", invernos);
        model.addAttribute("veraos",   veraos);
        return "quadro-resumo";
    }

    @PostMapping("/excluir")
    public String excluir(@RequestParam String colecao, RedirectAttributes redirectAttributes) {
        List<QuadroPlanejamento> linhas = repository.findByColecaoOrderByTipoSolicitacaoAsc(colecao);
        repository.deleteAll(linhas);
        redirectAttributes.addAttribute("aba", "quadro");
        return "redirect:/quadro-planejamento";
    }

    private int soma(Integer... vals) {
        int s = 0;
        for (Integer v : vals) if (v != null) s += v;
        return s;
    }

    @PostMapping
    public String salvar(@RequestParam String colecao,
                         @RequestParam List<String> tipoSolicitacao,
                         @RequestParam List<Integer> animeCotado,
                         @RequestParam List<Integer> animeAprovado,
                         @RequestParam List<Integer> animeCancelado,
                         @RequestParam List<Integer> momiCotado,
                         @RequestParam List<Integer> momiAprovado,
                         @RequestParam List<Integer> momiCancelado,
                         @RequestParam List<Integer> authoriaCotado,
                         @RequestParam List<Integer> authoriaAprovado,
                         @RequestParam List<Integer> authoriaCancelado,
                         @RequestParam List<Integer> bimbiCotado,
                         @RequestParam List<Integer> bimbiAprovado,
                         @RequestParam List<Integer> bimbiCancelado,
                         @RequestParam List<Integer> youcciecotado,
                         @RequestParam List<Integer> youccieeAprovado,
                         @RequestParam List<Integer> youcciecancelado,
                         RedirectAttributes redirectAttributes) {

        for (int i = 0; i < tipoSolicitacao.size(); i++) {
            String tipo = tipoSolicitacao.get(i);
            QuadroPlanejamento q = repository
                .findByColecaoAndTipoSolicitacao(colecao, tipo)
                .orElseGet(() -> {
                    QuadroPlanejamento novo = new QuadroPlanejamento();
                    novo.setColecao(colecao);
                    novo.setTipoSolicitacao(tipo);
                    return novo;
                });

            q.setAnimeCotado(animeCotado.get(i));
            q.setAnimeAprovado(animeAprovado.get(i));
            q.setAnimeCancelado(animeCancelado.get(i));
            q.setMomiCotado(momiCotado.get(i));
            q.setMomiAprovado(momiAprovado.get(i));
            q.setMomiCancelado(momiCancelado.get(i));
            q.setAuthoriaCotado(authoriaCotado.get(i));
            q.setAuthoriaAprovado(authoriaAprovado.get(i));
            q.setAuthoriaCancelado(authoriaCancelado.get(i));
            q.setBimbiCotado(bimbiCotado.get(i));
            q.setBimbiAprovado(bimbiAprovado.get(i));
            q.setBimbiCancelado(bimbiCancelado.get(i));
            q.setYoucciecotado(youcciecotado.get(i));
            q.setYouccieeAprovado(youccieeAprovado.get(i));
            q.setYoucciecancelado(youcciecancelado.get(i));

            repository.save(q);
        }

        redirectAttributes.addAttribute("colecao", colecao);
        return "redirect:/quadro-planejamento";
    }
}
