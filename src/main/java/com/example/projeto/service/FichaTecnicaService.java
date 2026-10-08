package com.example.projeto.service;

import com.example.projeto.dto.EmbarquePo;
import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.repository.FichaTecnicaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FichaTecnicaService {

    private final FichaTecnicaRepository repository;
    private final Path uploadDir;

    public FichaTecnicaService(FichaTecnicaRepository repository,
                                @Value("${app.upload.dir}") String uploadPath) {
        this.repository = repository;
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar diretório de uploads", e);
        }
    }

    public List<FichaTecnica> listarTodas() {
        return repository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "id"));
    }

    public FichaTecnica buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ficha técnica não encontrada: " + id));
    }

    public FichaTecnica salvar(FichaTecnica ficha, MultipartFile foto) {
        // gramatura é opcional, não há restrição por tipo
        if (foto != null && !foto.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + foto.getOriginalFilename();
            try {
                Files.copy(foto.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                ficha.setFotoPath(filename);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar foto", e);
            }
        }
        return repository.save(ficha);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public List<FichaTecnica> buscarComFiltros(String colecao,
                                                String tipo, StatusPedido statusPedido,
                                                LocalDate dataInicio, LocalDate dataFim,
                                                String duimpDi, String contratoCambio,
                                                String codigo, String numeroPedido,
                                                String fornecedor) {
        // O campo de Nº Pedido aceita vários pedidos de uma vez. A @Query tem um parâmetro
        // só, então com mais de um número a consulta vai sem esse filtro e a seleção é feita
        // aqui, casando qualquer um deles. A base é pequena, não compensa montar query dinâmica.
        List<String> pedidos = separarPedidos(numeroPedido);
        String filtroPedido = (pedidos.size() == 1) ? pedidos.get(0) : null;

        List<FichaTecnica> resultado = repository.buscarComFiltros(
                emptyToNull(colecao),
                emptyToNull(tipo),
                statusPedido,
                dataInicio,
                dataFim,
                emptyToNull(duimpDi),
                emptyToNull(contratoCambio),
                emptyToNull(codigo),
                emptyToNull(filtroPedido),
                emptyToNull(fornecedor));

        if (pedidos.size() > 1) {
            resultado = resultado.stream()
                    .filter(f -> combinaComAlgumPedido(f.getNumeroPedido(), pedidos))
                    .toList();
        }
        return resultado;
    }

    /**
     * Quebra o campo de Nº Pedido em vários números: aceita vírgula, ponto e vírgula,
     * espaço ou quebra de linha como separador, para dar certo tanto digitando
     * "1234, 5678" quanto colando uma coluna inteira do Excel.
     */
    public static List<String> separarPedidos(String entrada) {
        if (entrada == null || entrada.isBlank()) return List.of();
        return java.util.Arrays.stream(entrada.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    /** Mesma regra do filtro de um pedido só: casa por trecho, sem diferenciar maiúscula. */
    private boolean combinaComAlgumPedido(String numeroPedido, List<String> pedidos) {
        if (numeroPedido == null) return false;
        String alvo = numeroPedido.toLowerCase();
        for (String pedido : pedidos) {
            if (alvo.contains(pedido.toLowerCase())) return true;
        }
        return false;
    }

    public java.util.Map<String, Long> qtdPorColecao() {
        List<Object[]> rows = new ArrayList<>(repository.countByColecao());
        rows.sort((a, b) -> {
            int yearA = normalizeYear(extractTrailingNumber((String) a[0]));
            int yearB = normalizeYear(extractTrailingNumber((String) b[0]));
            if (yearA != yearB) return Integer.compare(yearB, yearA);
            return ((String) a[0]).compareToIgnoreCase((String) b[0]);
        });
        java.util.Map<String, Long> resultado = new java.util.LinkedHashMap<>();
        for (Object[] row : rows) {
            resultado.put((String) row[0], (Long) row[1]);
        }
        return resultado;
    }

    private static int extractTrailingNumber(String s) {
        Matcher m = Pattern.compile("(\\d+)\\s*$").matcher(s.trim());
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static int normalizeYear(int y) {
        return (y >= 0 && y < 100) ? 2000 + y : y;
    }


    /**
     * Mesma informação de {@link #qtdTipoPorColecao()}, mas em mapa plano com chave "colecao|tipo".
     * O Thymeleaf não resolve acesso aninhado (mapa[var][var]) quando as chaves vêm de variáveis
     * do th:each, então a listagem usa esta versão para montar as colunas por insumo.
     */
    public java.util.Map<String, Long> qtdPorColecaoETipoPlano() {
        java.util.Map<String, Long> resultado = new java.util.LinkedHashMap<>();
        for (Object[] row : repository.countByColecaoAndTipo()) {
            resultado.put(row[0] + "|" + row[1], (Long) row[2]);
        }
        return resultado;
    }

    /**
     * Como {@link #qtdPorColecaoETipoPlano()}, mas só com itens não cancelados, para o resumo
     * da listagem de fichas separar os cancelados numa coluna própria.
     */
    public java.util.Map<String, Long> qtdAtivosPorColecaoETipoPlano() {
        java.util.Map<String, Long> resultado = new java.util.LinkedHashMap<>();
        for (FichaTecnica f : repository.findAll()) {
            if (f.isCancelado() || f.getColecao() == null || f.getColecao().isEmpty()) continue;
            resultado.merge(f.getColecao() + "|" + f.getTipo(), 1L, Long::sum);
        }
        return resultado;
    }

    /** Itens cancelados por coleção, pela mesma regra de {@link FichaTecnica#isCancelado()}. */
    public java.util.Map<String, Long> qtdCanceladosPorColecao() {
        java.util.Map<String, Long> resultado = new java.util.LinkedHashMap<>();
        for (FichaTecnica f : repository.findAll()) {
            if (!f.isCancelado() || f.getColecao() == null || f.getColecao().isEmpty()) continue;
            resultado.merge(f.getColecao(), 1L, Long::sum);
        }
        return resultado;
    }

    /** Insumos distintos realmente usados pelas fichas (usado como colunas da listagem). */
    public java.util.List<String> tiposUsados() {
        java.util.List<String> tipos = new ArrayList<>();
        for (Object[] row : repository.countByColecaoAndTipo()) {
            String tipo = (String) row[1];
            if (tipo != null && !tipo.isBlank() && !tipos.contains(tipo)) tipos.add(tipo);
        }
        java.util.Collections.sort(tipos);
        return tipos;
    }

    public java.util.Map<String, java.util.Map<String, Long>> qtdTipoPorColecao() {
        java.util.Map<String, java.util.Map<String, Long>> resultado = new java.util.LinkedHashMap<>();
        for (Object[] row : repository.countByColecaoAndTipo()) {
            String colecao = (String) row[0];
            String tipo = (String) row[1];
            Long count = (Long) row[2];
            resultado.computeIfAbsent(colecao, k -> new java.util.LinkedHashMap<>()).put(tipo, count);
        }
        return resultado;
    }

    /**
     * Retorna Map<colecao, Map<tipo, Map<status, count>>> para o resumo de amostras cor/qualidade.
     * Ordenado por ano decrescente (mais recente primeiro), depois por tipo.
     */
    public Map<String, Map<String, Map<String, Long>>> resumoStatusProducaoPorColecaoETipo() {
        List<Object[]> rows = new ArrayList<>(repository.countByColecaoAndTipoAndStatusAmostraProducao());
        rows.sort((a, b) -> {
            String ca = (String) a[0], cb = (String) b[0];
            int ya = normalizeYear(extractTrailingNumber(ca));
            int yb = normalizeYear(extractTrailingNumber(cb));
            if (ya != yb) return Integer.compare(yb, ya);
            int cmp = ca.compareToIgnoreCase(cb);
            if (cmp != 0) return cmp;
            return ((String) a[1]).compareToIgnoreCase((String) b[1]);
        });
        Map<String, Map<String, Map<String, Long>>> resultado = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String colecao = (String) row[0];
            String tipo    = (String) row[1];
            String status  = ((com.example.projeto.model.StatusAmostra) row[2]).name();
            Long   count   = (Long) row[3];
            resultado
                .computeIfAbsent(colecao, k -> new LinkedHashMap<>())
                .computeIfAbsent(tipo,    k -> new LinkedHashMap<>())
                .put(status, count);
        }
        return resultado;
    }

    public Map<String, Map<String, Map<String, Long>>> resumoStatusCorPorColecaoETipo() {
        List<Object[]> rows = new ArrayList<>(repository.countByColecaoAndTipoAndStatusAmostraCor());
        // Ordena: ano decrescente, depois colecao alfabético, depois tipo
        rows.sort((a, b) -> {
            String ca = (String) a[0], cb = (String) b[0];
            int ya = normalizeYear(extractTrailingNumber(ca));
            int yb = normalizeYear(extractTrailingNumber(cb));
            if (ya != yb) return Integer.compare(yb, ya);
            int cmp = ca.compareToIgnoreCase(cb);
            if (cmp != 0) return cmp;
            return ((String) a[1]).compareToIgnoreCase((String) b[1]);
        });
        Map<String, Map<String, Map<String, Long>>> resultado = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String colecao = (String) row[0];
            String tipo    = (String) row[1];
            String status  = ((com.example.projeto.model.StatusAmostra) row[2]).name();
            Long   count   = (Long) row[3];
            resultado
                .computeIfAbsent(colecao, k -> new LinkedHashMap<>())
                .computeIfAbsent(tipo,    k -> new LinkedHashMap<>())
                .put(status, count);
        }
        return resultado;
    }

    public java.util.Map<String, java.util.Map<String, Long>> qtdStatusProducaoPorColecao() {
        java.util.Map<String, java.util.Map<String, Long>> resultado = new java.util.LinkedHashMap<>();
        for (Object[] row : repository.countByColecaoAndStatusAmostraProducao()) {
            String colecao = (String) row[0];
            String status = ((com.example.projeto.model.StatusAmostra) row[1]).name();
            Long count = (Long) row[2];
            resultado.computeIfAbsent(colecao, k -> new java.util.LinkedHashMap<>()).put(status, count);
        }
        return resultado;
    }

    public java.util.Map<String, java.util.Map<String, Long>> qtdStatusCorPorColecao() {
        java.util.Map<String, java.util.Map<String, Long>> resultado = new java.util.LinkedHashMap<>();
        for (Object[] row : repository.countByColecaoAndStatusAmostraCor()) {
            String colecao = (String) row[0];
            String status = ((com.example.projeto.model.StatusAmostra) row[1]).name();
            Long count = (Long) row[2];
            resultado.computeIfAbsent(colecao, k -> new java.util.LinkedHashMap<>()).put(status, count);
        }
        return resultado;
    }

    /** Pivot: fornecedor → (colecao → count). Usado nos painéis de aprovação. */
    public Map<String, Map<String, Long>> qtdFornecedorPorColecao() {
        Map<String, Map<String, Long>> resultado = new LinkedHashMap<>();
        for (Object[] row : repository.countByFornecedorAndColecao()) {
            String fornecedor = (String) row[0];
            String colecao   = (String) row[1];
            Long   count     = (Long)   row[2];
            resultado.computeIfAbsent(fornecedor, k -> new LinkedHashMap<>()).put(colecao, count);
        }
        return resultado;
    }

    /** Total geral por fornecedor (soma de todas as coleções). */
    public Map<String, Long> totalPorFornecedor() {
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (Object[] row : repository.countByFornecedorAndColecao()) {
            String fornecedor = (String) row[0];
            Long   count      = (Long)   row[2];
            resultado.merge(fornecedor, count, Long::sum);
        }
        return resultado;
    }

    public List<Map<String, Object>> leadtimeAprovacaoCorPorMarca() {
        // Agrupa por coleção + marca, calcula leadtime médio/min/max
        Map<String, List<Long>> porChave = new LinkedHashMap<>();
        Map<String, String[]> metadados = new LinkedHashMap<>();
        for (FichaTecnica f : repository.findComLeadtimeAprovacaoCor()) {
            long dias = calcularDiasUteis(f.getDataColocacaoPedido(), f.getDataAprovacaoAmostraCor());
            String colecao = f.getColecao() != null ? f.getColecao() : "-";
            String marca = f.getMarca() != null ? f.getMarca() : "-";
            String chave = colecao + "||" + marca;
            porChave.computeIfAbsent(chave, k -> new java.util.ArrayList<>()).add(dias);
            metadados.putIfAbsent(chave, new String[]{colecao, marca});
        }
        List<Map<String, Object>> resultado = new java.util.ArrayList<>();
        porChave.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    List<Long> dias = e.getValue();
                    long media = Math.round(dias.stream().mapToLong(Long::longValue).average().orElse(0));
                    long min = dias.stream().mapToLong(Long::longValue).min().orElse(0);
                    long max = dias.stream().mapToLong(Long::longValue).max().orElse(0);
                    String[] meta = metadados.get(e.getKey());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("colecao", meta[0]);
                    row.put("marca", meta[1]);
                    row.put("qtd", dias.size());
                    row.put("mediaLeadtime", media);
                    row.put("minLeadtime", min);
                    row.put("maxLeadtime", max);
                    resultado.add(row);
                });
        return resultado;
    }

    public List<Map<String, Object>> leadtimeAprovacaoProducaoPorMarca() {
        Map<String, List<Long>> porChave = new LinkedHashMap<>();
        Map<String, String[]> metadados = new LinkedHashMap<>();
        for (FichaTecnica f : repository.findComLeadtimeAprovacaoProducao()) {
            long dias = calcularDiasUteis(f.getDataColocacaoPedido(), f.getDataAprovacaoAmostraProducao());
            String colecao = f.getColecao() != null ? f.getColecao() : "-";
            String marca = f.getMarca() != null ? f.getMarca() : "-";
            String chave = colecao + "||" + marca;
            porChave.computeIfAbsent(chave, k -> new java.util.ArrayList<>()).add(dias);
            metadados.putIfAbsent(chave, new String[]{colecao, marca});
        }
        List<Map<String, Object>> resultado = new java.util.ArrayList<>();
        porChave.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    List<Long> dias = e.getValue();
                    long media = Math.round(dias.stream().mapToLong(Long::longValue).average().orElse(0));
                    long min = dias.stream().mapToLong(Long::longValue).min().orElse(0);
                    long max = dias.stream().mapToLong(Long::longValue).max().orElse(0);
                    String[] meta = metadados.get(e.getKey());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("colecao", meta[0]);
                    row.put("marca", meta[1]);
                    row.put("mediaLeadtime", media);
                    row.put("minLeadtime", min);
                    row.put("maxLeadtime", max);
                    resultado.add(row);
                });
        return resultado;
    }

    /** Conta os dias úteis entre as duas datas, sem sábados, domingos e feriados (ver {@link Feriados}). */
    private long calcularDiasUteis(LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio)) return 0;
        long dias = 0;
        LocalDate data = inicio;
        while (!data.isAfter(fim)) {
            if (Feriados.isDiaUtil(data)) {
                dias++;
            }
            data = data.plusDays(1);
        }
        return dias;
    }

    /** Rótulo dos itens que ainda não têm número de pedido preenchido. */
    public static final String SEM_PEDIDO = "(sem nº de pedido)";

    private static final Locale BR = Locale.of("pt", "BR");

    /**
     * Agrupa as fichas por número de pedido para a aba "Embarque por PO" da tela de Pedidos.
     *
     * Recebe a lista já filtrada pela tela em vez de ir ao banco de novo, para que o resumo
     * acompanhe exatamente o recorte que está sendo mostrado na aba de itens.
     *
     * Um PO embarcado em partes tem mais de um navio: a linha traz a menor data de saída e a
     * maior data de chegada (o intervalo do pedido inteiro) e guarda os itens para o detalhe.
     * Itens cancelados entram numa contagem própria, mas ficam fora da quantidade e do valor,
     * mesma regra do total da aba de itens e de /quadro-compras.
     */
    public List<EmbarquePo> resumoEmbarquePorPo(List<FichaTecnica> fichas) {
        Map<String, List<FichaTecnica>> porPedido = new LinkedHashMap<>();
        for (FichaTecnica f : fichas) {
            String po = (f.getNumeroPedido() == null || f.getNumeroPedido().isBlank())
                    ? SEM_PEDIDO : f.getNumeroPedido().trim();
            porPedido.computeIfAbsent(po, k -> new ArrayList<>()).add(f);
        }

        LocalDate hoje = LocalDate.now();
        List<EmbarquePo> resumo = new ArrayList<>();

        for (Map.Entry<String, List<FichaTecnica>> entry : porPedido.entrySet()) {
            List<FichaTecnica> itens = entry.getValue();
            EmbarquePo linha = new EmbarquePo();
            linha.setNumeroPedido(entry.getKey());
            linha.setItensLista(itens);
            linha.setItens(itens.size());

            Set<String> fornecedores = new LinkedHashSet<>();
            Set<String> colecoes = new LinkedHashSet<>();
            Set<String> navios = new LinkedHashSet<>();
            Set<String> status = new LinkedHashSet<>();
            Map<String, Double> qtdPorUnidade = new LinkedHashMap<>();
            LocalDate saida = null;
            LocalDate chegada = null;
            double valorUsd = 0;
            boolean temValor = false;
            int cancelados = 0;

            for (FichaTecnica f : itens) {
                adicionarSeTiver(fornecedores, f.getFornecedor());
                adicionarSeTiver(colecoes, f.getColecao());
                adicionarSeTiver(navios, f.getNomeNavio());
                if (f.getStatusPedido() != null) status.add(f.getStatusPedido().getDescricao());

                // As datas de embarque valem mesmo para item cancelado: o navio zarpou com o
                // resto do pedido, e ignorá-las deixaria o PO sem data na tela.
                if (f.getDataSaidaOrigem() != null && (saida == null || f.getDataSaidaOrigem().isBefore(saida))) {
                    saida = f.getDataSaidaOrigem();
                }
                if (f.getDataChegadaDestino() != null && (chegada == null || f.getDataChegadaDestino().isAfter(chegada))) {
                    chegada = f.getDataChegadaDestino();
                }

                if (f.isCancelado()) {
                    cancelados++;
                    continue;
                }
                if (f.getQuantidadeComprada() != null) {
                    String unidade = (f.getUnidadeMedida() == null || f.getUnidadeMedida().isBlank())
                            ? "-" : f.getUnidadeMedida();
                    qtdPorUnidade.merge(unidade, f.getQuantidadeComprada(), Double::sum);
                    if (f.getPrecoUsd() != null) {
                        valorUsd += f.getPrecoUsd() * f.getQuantidadeComprada();
                        temValor = true;
                    }
                }
            }

            linha.setFornecedores(juntar(fornecedores));
            linha.setColecoes(juntar(colecoes));
            linha.setNavios(juntar(navios));
            linha.setQtdNavios(navios.size());
            linha.setStatusPedidos(juntar(status));
            linha.setItensCancelados(cancelados);
            linha.setSaida(saida);
            linha.setChegada(chegada);
            linha.setQtdResumo(formatarQuantidades(qtdPorUnidade));
            linha.setValorUsd(temValor ? valorUsd : null);

            if (saida != null && chegada != null && !chegada.isBefore(saida)) {
                linha.setDiasTransito((int) ChronoUnit.DAYS.between(saida, chegada));
            }
            if (chegada != null) {
                linha.setDiasParaChegar((int) ChronoUnit.DAYS.between(hoje, chegada));
            }

            if (chegada != null && !chegada.isAfter(hoje)) {
                definirSituacao(linha, "CHEGADO", "Chegou", "success");
            } else if (saida != null && !saida.isAfter(hoje)) {
                definirSituacao(linha, "EM_TRANSITO", "Em trânsito", "info text-dark");
            } else if (saida != null || chegada != null) {
                definirSituacao(linha, "PREVISTO", "Previsto", "primary");
            } else {
                definirSituacao(linha, "SEM_DATA", "Sem data", "secondary");
            }

            resumo.add(linha);
        }

        // Quem embarcou primeiro aparece primeiro; PO sem data de saída vai para o fim da lista.
        resumo.sort((a, b) -> {
            int cmp = compararDatasNulasNoFim(a.getSaida(), b.getSaida());
            if (cmp != 0) return cmp;
            cmp = compararDatasNulasNoFim(a.getChegada(), b.getChegada());
            if (cmp != 0) return cmp;
            return a.getNumeroPedido().compareToIgnoreCase(b.getNumeroPedido());
        });
        return resumo;
    }

    private void definirSituacao(EmbarquePo linha, String chave, String label, String cor) {
        linha.setSituacao(chave);
        linha.setSituacaoLabel(label);
        linha.setSituacaoCor(cor);
    }

    private void adicionarSeTiver(Set<String> destino, String valor) {
        if (valor != null && !valor.isBlank()) destino.add(valor.trim());
    }

    private String juntar(Set<String> valores) {
        return valores.isEmpty() ? null : String.join(", ", valores);
    }

    private static int compararDatasNulasNoFim(LocalDate a, LocalDate b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    /** "1.200,00 MT · 80,00 KG" — uma soma por unidade, porque metro e quilo não se somam. */
    private String formatarQuantidades(Map<String, Double> qtdPorUnidade) {
        if (qtdPorUnidade.isEmpty()) return null;
        List<String> partes = new ArrayList<>();
        for (Map.Entry<String, Double> e : qtdPorUnidade.entrySet()) {
            partes.add(String.format(BR, "%,.2f", e.getValue()) + " " + abreviarUnidade(e.getKey()));
        }
        return String.join(" · ", partes);
    }

    private String abreviarUnidade(String unidade) {
        return switch (unidade) {
            case "Metro" -> "MT";
            case "Quilo" -> "KG";
            case "Unidade" -> "UN";
            default -> unidade;
        };
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
