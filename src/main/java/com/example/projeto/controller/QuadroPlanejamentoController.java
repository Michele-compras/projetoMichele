package com.example.projeto.controller;

import com.example.projeto.model.BandeiraNovidade;
import com.example.projeto.model.QuadroPlanejamento;
import com.example.projeto.repository.BandeiraNovidadeRepository;
import com.example.projeto.repository.ColecaoRepository;
import com.example.projeto.repository.FornecedorRepository;
import com.example.projeto.repository.InsumoRepository;
import com.example.projeto.repository.QuadroPlanejamentoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quadro-planejamento")
public class QuadroPlanejamentoController {

    private final QuadroPlanejamentoRepository repository;
    private final ColecaoRepository colecaoRepo;
    private final InsumoRepository insumoRepo;
    private final FornecedorRepository fornecedorRepo;
    private final BandeiraNovidadeRepository bandeiraRepo;

    public QuadroPlanejamentoController(QuadroPlanejamentoRepository repository,
                                        ColecaoRepository colecaoRepo,
                                        InsumoRepository insumoRepo,
                                        FornecedorRepository fornecedorRepo,
                                        BandeiraNovidadeRepository bandeiraRepo) {
        this.repository = repository;
        this.colecaoRepo = colecaoRepo;
        this.insumoRepo = insumoRepo;
        this.fornecedorRepo = fornecedorRepo;
        this.bandeiraRepo = bandeiraRepo;
    }

    @GetMapping
    public String exibir(@RequestParam(required = false) String colecao,
                         @RequestParam(required = false, defaultValue = "quadro") String aba,
                         Model model) {
        List<String> colecoes = colecaoRepo.findAll().stream()
                .map(c -> c.getNome()).toList();

        if (colecao == null || colecao.isBlank()) {
            colecao = colecoes.isEmpty() ? "" : colecoes.get(0);
        }

        List<String> insumos = insumoRepo.findAll().stream().map(i -> i.getNome()).toList();

        // Eliminar registros cujo tipoSolicitacao não existe no Cadastro Prévio
        List<QuadroPlanejamento> todos = repository.findAll();
        for (QuadroPlanejamento q : todos) {
            if (!insumos.contains(q.getTipoSolicitacao())) {
                repository.delete(q);
            }
        }

        final String colecaoFinal = colecao;
        List<QuadroPlanejamento> linhas = new ArrayList<>();
        for (String tipo : insumos) {
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
                if (!insumos.contains(q.getTipoSolicitacao())) continue;
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

        // Dados para a aba Bandeiras/Novidades
        List<String> fornecedores = fornecedorRepo.findAll().stream().map(f -> f.getNome()).toList();

        // Map plano: "colecao|insumo|fornecedor" -> quantidade
        Map<String, Integer> bandeirasFlat = new LinkedHashMap<>();
        for (String col : colecoes) {
            for (String ins : insumos) {
                for (String forn : fornecedores) {
                    Integer qtd = bandeiraRepo.findByColecaoAndFornecedorAndInsumo(col, forn, ins)
                            .map(BandeiraNovidade::getQuantidade).orElse(0);
                    bandeirasFlat.put(col + "|" + ins + "|" + forn, qtd != null ? qtd : 0);
                }
            }
        }

        System.out.println("[GET] bandeirasFlat com qtd > 0:");
        bandeirasFlat.forEach((k, v) -> { if (v != null && v > 0) System.out.println("[GET] " + k + " = " + v); });
        model.addAttribute("linhas", linhas);
        model.addAttribute("colecaoAtual", colecaoFinal);
        model.addAttribute("colecoes", colecoes);
        model.addAttribute("insumos", insumos);
        model.addAttribute("invernos", invernos);
        model.addAttribute("veraos", veraos);
        model.addAttribute("abaAtiva", aba);
        model.addAttribute("fornecedores", fornecedores);
        model.addAttribute("bandeirasFlat", bandeirasFlat);
        return "quadro-planejamento";
    }

    @GetMapping("/resumo")
    public String resumo(Model model) {
        List<String> colecoes = colecaoRepo.findAll().stream()
                .map(c -> c.getNome()).toList();
        List<String> insumos = insumoRepo.findAll().stream().map(i -> i.getNome()).toList();

        List<java.util.Map<String, Object>> invernos = new ArrayList<>();
        List<java.util.Map<String, Object>> veraos   = new ArrayList<>();

        for (String col : colecoes) {
            List<QuadroPlanejamento> linhas = repository.findByColecaoOrderByTipoSolicitacaoAsc(col);
            int cotado = 0, aprovado = 0, cancelado = 0;
            for (QuadroPlanejamento q : linhas) {
                if (!insumos.contains(q.getTipoSolicitacao())) continue;
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

    @PostMapping("/bandeiras")
    public String salvarBandeiras(@RequestParam(required = false) List<String> bandColecao,
                                   @RequestParam(required = false) List<String> bandInsumo,
                                   @RequestParam(required = false) List<String> bandFornecedor,
                                   @RequestParam(required = false) List<String> bandQtd,
                                   RedirectAttributes redirectAttributes) {
        System.out.println("[BANDEIRAS] bandColecao=" + bandColecao);
        System.out.println("[BANDEIRAS] bandInsumo=" + bandInsumo);
        System.out.println("[BANDEIRAS] bandFornecedor=" + bandFornecedor);
        System.out.println("[BANDEIRAS] bandQtd=" + bandQtd);
        if (bandColecao == null || bandColecao.isEmpty()) {
            redirectAttributes.addAttribute("aba", "bandeiras");
            return "redirect:/quadro-planejamento";
        }
        for (int i = 0; i < bandColecao.size(); i++) {
            final String col  = bandColecao.get(i);
            final String ins  = bandInsumo.get(i);
            final String forn = bandFornecedor.get(i);
            final String qtdStr = (bandQtd != null && i < bandQtd.size()) ? bandQtd.get(i) : "0";
            int qtd = 0;
            try { qtd = Integer.parseInt(qtdStr == null || qtdStr.isBlank() ? "0" : qtdStr.trim()); } catch (NumberFormatException ignored) {}
            final int qtdFinal = qtd;
            System.out.println("[BANDEIRAS] Salvando: col=" + col + " ins=" + ins + " forn=" + forn + " qtd=" + qtdFinal);
            BandeiraNovidade bn = bandeiraRepo
                    .findByColecaoAndFornecedorAndInsumo(col, forn, ins)
                    .orElseGet(() -> {
                        BandeiraNovidade novo = new BandeiraNovidade();
                        novo.setColecao(col);
                        novo.setFornecedor(forn);
                        novo.setInsumo(ins);
                        return novo;
                    });
            bn.setQuantidade(qtdFinal);
            bandeiraRepo.save(bn);
            System.out.println("[BANDEIRAS] Salvo id=" + bn.getId() + " qtd=" + bn.getQuantidade());
        }
        redirectAttributes.addAttribute("aba", "bandeiras");
        return "redirect:/quadro-planejamento";
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
