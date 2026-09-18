package com.example.projeto.controller;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.QuadroPlanejamento;
import com.example.projeto.repository.ColecaoRepository;
import com.example.projeto.repository.FichaTecnicaRepository;
import com.example.projeto.repository.InsumoRepository;
import com.example.projeto.repository.MarcaRepository;
import com.example.projeto.repository.QuadroPlanejamentoRepository;
import com.example.projeto.service.FichaTecnicaService;
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
    private final FichaTecnicaRepository fichaRepo;
    private final FichaTecnicaService fichaService;
    private final MarcaRepository marcaRepo;

    public GraficosController(QuadroPlanejamentoRepository quadroRepo,
                              ColecaoRepository colecaoRepo,
                              InsumoRepository insumoRepo,
                              FichaTecnicaRepository fichaRepo,
                              FichaTecnicaService fichaService,
                              MarcaRepository marcaRepo) {
        this.quadroRepo = quadroRepo;
        this.colecaoRepo = colecaoRepo;
        this.insumoRepo = insumoRepo;
        this.fichaRepo = fichaRepo;
        this.fichaService = fichaService;
        this.marcaRepo = marcaRepo;
    }

    @GetMapping
    public String exibir(@RequestParam(required = false) String colecaoFiltro,
                         @RequestParam(required = false, defaultValue = "cotacao") String aba,
                         Model model) {
        // O filtro é uma lista das coleções cadastradas no Cadastro Prévio (tabela
        // Colecao), e não texto livre: assim só se filtra por coleção que existe.
        var colecoesCadastradas = colecaoRepo.findAll();
        model.addAttribute("colecoesCadastradas", colecoesCadastradas);

        String selecionada = colecaoFiltro != null ? colecaoFiltro.trim() : "";
        model.addAttribute("colecaoFiltro", selecionada);
        model.addAttribute("abaAtiva", aba);

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

        // ── Gráfico 2: Comprado por Coleção, dividido por insumo ──────────────
        // As categorias pedidas (tecido mtr, aviamento mtr, aviamento unidade) já são
        // os proprios insumos cadastrados, então as séries saem da tabela Insumo em
        // vez de serem fixas no código: cadastrou insumo novo, ele aparece no gráfico.
        model.addAttribute("blocosComprado", montarBlocosComprado(insumos, selecionada));

        // ── Gráfico 3: Leadtime médio da amostra de produção ──────────────────
        model.addAttribute("blocosLeadtime", montarBlocosLeadtime(selecionada));
        // Ordem das marcas cadastradas: é ela que define a cor de cada barra. Por
        // vir do cadastro, e não da ordenação do gráfico, a marca mantém a mesma
        // cor mesmo quando muda de posição por ter ficado mais lenta ou mais rápida.
        model.addAttribute("marcasCadastradas", marcaRepo.findAll().stream()
                .map(m -> m.getNome()).toList());

        // ── Gráfico 4: Peças desenvolvidas por material ───────────────────────
        model.addAttribute("pecasDemo", montarPecasDemo());

        return "graficos";
    }

    /**
     * DADOS FICTÍCIOS — protótipo.
     *
     * O sistema não registra quantas peças são desenvolvidas com cada tecido ou
     * aviamento: não existe esse campo na ficha técnica. Este método devolve um
     * conjunto inventado só para a tela poder ser avaliada.
     *
     * Para virar real, é preciso um campo de "peças desenvolvidas" na ficha (o
     * campo "Qti pct" existe mas é texto livre e não é usado em nenhuma tela) e
     * este método passa a ler do banco. Enquanto for demonstração, a aba avisa
     * isso em vermelho e nada aqui toca o banco.
     */
    private List<Map<String, Object>> montarPecasDemo() {
        List<Map<String, Object>> linhas = new ArrayList<>();
        linhas.add(linhaPecaDemo("T5444.001.000001", "TECIDO METRO",     "ANIMÊ",    7));
        linhas.add(linhaPecaDemo("T4985.001.BG0009", "TECIDO METRO",     "ANIMÊ",    4));
        linhas.add(linhaPecaDemo("T5554.001.BG0009", "TECIDO METRO",     "AUTHORIA", 6));
        linhas.add(linhaPecaDemo("T5102.001.000003", "TECIDO METRO",     "MOMI",     9));
        linhas.add(linhaPecaDemo("T5310.001.000002", "TECIDO METRO",     "BIMBI",    3));
        linhas.add(linhaPecaDemo("T5471.001.000004", "TECIDO METRO",     "YOUCCIE",  5));
        linhas.add(linhaPecaDemo("PI345.001.000001", "AVIAMENTO METRO",  "ANIMÊ",    2));
        linhas.add(linhaPecaDemo("PI338.001.000001", "AVIAMENTO METRO",  "AUTHORIA", 3));
        linhas.add(linhaPecaDemo("PI336.001.000001", "AVIAMENTO METRO",  "MOMI",     4));
        linhas.add(linhaPecaDemo("AV220.001.000007", "AVIAMENTO UNIDADE","ANIMÊ",    5));
        linhas.add(linhaPecaDemo("AV118.001.000002", "AVIAMENTO UNIDADE","BIMBI",    6));
        linhas.add(linhaPecaDemo("AV305.001.000005", "AVIAMENTO UNIDADE","YOUCCIE",  2));
        return linhas;
    }

    private Map<String, Object> linhaPecaDemo(String codigo, String insumo, String marca, int pecas) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("codigo", codigo);
        m.put("insumo", insumo);
        // "Tecido" ou "Aviamento": é a divisão que o gráfico usa nas séries.
        m.put("grupo", insumo.startsWith("TECIDO") ? "Tecido" : "Aviamento");
        m.put("marca", marca);
        m.put("pecas", pecas);
        return m;
    }

    /**
     * Leadtime médio, por coleção/marca, entre a colocação do pedido e a aprovação
     * da amostra de produção. Usa o mesmo cálculo da tela de Aprovação/Embarque
     * (service.leadtimeAprovacaoProducaoPorMarca), que conta DIAS ÚTEIS e só
     * considera fichas com marca e com as duas datas preenchidas.
     */
    private List<Map<String, Object>> montarBlocosLeadtime(String selecionada) {
        List<Map<String, Object>> verao   = new ArrayList<>();
        List<Map<String, Object>> inverno = new ArrayList<>();

        for (Map<String, Object> linha : fichaService.leadtimeAprovacaoProducaoPorMarca()) {
            String col = (String) linha.get("colecao");
            if (col == null) continue;
            if (!selecionada.isBlank() && !selecionada.equals(col)) continue;
            (col.toLowerCase().contains("inverno") ? inverno : verao).add(linha);
        }

        List<Map<String, Object>> blocos = new ArrayList<>();
        blocos.add(blocoLeadtime("Verão", verao));
        blocos.add(blocoLeadtime("Inverno", inverno));
        return blocos;
    }

    private Map<String, Object> blocoLeadtime(String titulo, List<Map<String, Object>> linhas) {
        // Maior média primeiro: num gráfico de leadtime o que interessa é quem demora mais.
        linhas.sort((a, b) -> Long.compare(numero(b.get("mediaLeadtime")), numero(a.get("mediaLeadtime"))));

        // Quando o bloco tem uma coleção só, repetir o nome dela em toda barra é ruído;
        // com mais de uma, o nome é necessário para a marca não ficar ambígua.
        long distintas = linhas.stream().map(l -> (String) l.get("colecao")).distinct().count();

        List<String> labels  = new ArrayList<>();
        List<String> marcas  = new ArrayList<>();
        List<Long> medias    = new ArrayList<>();
        List<Long> minimos   = new ArrayList<>();
        List<Long> maximos   = new ArrayList<>();
        for (Map<String, Object> l : linhas) {
            labels.add(distintas > 1 ? l.get("colecao") + " · " + l.get("marca") : (String) l.get("marca"));
            // Marca crua, separada do rótulo: é por ela que o gráfico escolhe a cor.
            marcas.add((String) l.get("marca"));
            medias.add(numero(l.get("mediaLeadtime")));
            minimos.add(numero(l.get("minLeadtime")));
            maximos.add(numero(l.get("maxLeadtime")));
        }

        Map<String, Object> b = new LinkedHashMap<>();
        b.put("titulo", titulo);
        b.put("labels", labels);
        b.put("marcas", marcas);
        b.put("medias", medias);
        b.put("minimos", minimos);
        b.put("maximos", maximos);
        // Sem "média geral": a média das médias ignoraria quantas fichas há em cada
        // grupo e daria um número errado. Mostram-se os extremos, que são exatos.
        b.put("qtdGrupos", labels.size());
        b.put("menorMedia", medias.stream().mapToLong(Long::longValue).min().orElse(0));
        b.put("maiorMedia", medias.stream().mapToLong(Long::longValue).max().orElse(0));
        return b;
    }

    private long numero(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }

    /** Quantidade comprada por coleção, uma série por insumo, separada por estação. */
    private List<Map<String, Object>> montarBlocosComprado(List<String> insumos, String selecionada) {
        // Item cancelado não entra em quantidade comprada — mesma regra de
        // /quadro-compras, para os dois números não se contradizerem.
        List<FichaTecnica> fichas = fichaRepo.findAll().stream()
                .filter(f -> !f.isCancelado())
                .toList();

        List<String> veraoLabels   = new ArrayList<>();
        List<String> invernoLabels = new ArrayList<>();
        // insumo -> valores na ordem dos labels da estação
        Map<String, List<Double>> veraoSeries   = new LinkedHashMap<>();
        Map<String, List<Double>> invernoSeries = new LinkedHashMap<>();
        for (String ins : insumos) {
            veraoSeries.put(ins, new ArrayList<>());
            invernoSeries.put(ins, new ArrayList<>());
        }

        for (var colecao : colecaoRepo.findAll()) {
            String col = colecao.getNome();
            if (col == null) continue;
            if (!selecionada.isBlank() && !selecionada.equals(col)) continue;

            boolean inverno = col.toLowerCase().contains("inverno");
            (inverno ? invernoLabels : veraoLabels).add(col);
            Map<String, List<Double>> destino = inverno ? invernoSeries : veraoSeries;

            for (String ins : insumos) {
                double soma = 0;
                for (FichaTecnica f : fichas) {
                    if (!col.equals(f.getColecao())) continue;
                    if (!ins.equals(f.getTipo())) continue;
                    if (f.getQuantidadeComprada() == null) continue;
                    soma += f.getQuantidadeComprada();
                }
                destino.get(ins).add(soma);
            }
        }

        List<Map<String, Object>> blocos = new ArrayList<>();
        blocos.add(blocoComprado("Verão", veraoLabels, veraoSeries));
        blocos.add(blocoComprado("Inverno", invernoLabels, invernoSeries));
        return blocos;
    }

    private Map<String, Object> blocoComprado(String titulo, List<String> labels,
                                              Map<String, List<Double>> series) {
        List<Map<String, Object>> listaSeries = new ArrayList<>();
        double totalGeral = 0;
        for (var e : series.entrySet()) {
            double total = 0;
            for (Double v : e.getValue()) total += v != null ? v : 0;
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("nome", e.getKey());
            s.put("valores", e.getValue());
            s.put("total", total);
            listaSeries.add(s);
            totalGeral += total;
        }
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("titulo", titulo);
        b.put("labels", labels);
        b.put("series", listaSeries);
        b.put("totalGeral", totalGeral);
        return b;
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
