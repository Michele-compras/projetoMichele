package com.example.projeto.service;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
                                                String duimpDi, String contratoCambio) {
        return repository.buscarComFiltros(
                emptyToNull(colecao),
                emptyToNull(tipo),
                statusPedido,
                dataInicio,
                dataFim,
                emptyToNull(duimpDi),
                emptyToNull(contratoCambio));
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

    private long calcularDiasUteis(LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio)) return 0;
        long dias = 0;
        LocalDate data = inicio;
        while (!data.isAfter(fim)) {
            java.time.DayOfWeek dow = data.getDayOfWeek();
            if (dow != java.time.DayOfWeek.SATURDAY && dow != java.time.DayOfWeek.SUNDAY) {
                dias++;
            }
            data = data.plusDays(1);
        }
        return dias;
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
