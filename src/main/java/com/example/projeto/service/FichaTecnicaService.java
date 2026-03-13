package com.example.projeto.service;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.model.TipoItem;
import com.example.projeto.repository.FichaTecnicaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        if (ficha.getTipo() == TipoItem.ACESSORIO) {
            ficha.setGramatura(null);
        }
        if (foto != null && !foto.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + foto.getOriginalFilename();
            try {
                Files.copy(foto.getInputStream(), uploadDir.resolve(filename));
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
                                                TipoItem tipo, StatusPedido statusPedido,
                                                LocalDate dataInicio, LocalDate dataFim) {
        return repository.buscarComFiltros(
                emptyToNull(colecao),
                tipo,
                statusPedido,
                dataInicio,
                dataFim);
    }

    public java.util.Map<String, Long> qtdPorColecao() {
        java.util.Map<String, Long> resultado = new java.util.LinkedHashMap<>();
        for (Object[] row : repository.countByColecao()) {
            resultado.put((String) row[0], (Long) row[1]);
        }
        return resultado;
    }

    public java.util.Map<String, java.util.Map<String, Long>> qtdTipoPorColecao() {
        java.util.Map<String, java.util.Map<String, Long>> resultado = new java.util.LinkedHashMap<>();
        for (Object[] row : repository.countByColecaoAndTipo()) {
            String colecao = (String) row[0];
            String tipo = ((com.example.projeto.model.TipoItem) row[1]).name();
            Long count = (Long) row[2];
            resultado.computeIfAbsent(colecao, k -> new java.util.LinkedHashMap<>()).put(tipo, count);
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
            long dias = ChronoUnit.DAYS.between(f.getDataColocacaoPedido(), f.getDataAprovacaoAmostraCor());
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

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
