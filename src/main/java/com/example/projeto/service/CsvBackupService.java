package com.example.projeto.service;

import com.example.projeto.model.*;
import com.example.projeto.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Faz backup periódico (e sob demanda) de todas as entidades em arquivos .csv.
 * O banco H2 continua sendo a fonte principal; o CSV é uma cópia de segurança
 * legível, para os dados não se perderem enquanto o sistema está em testes.
 *
 * Usa reflection sobre os getters de cada entidade, então novos campos passam
 * a ser exportados automaticamente, sem precisar alterar este serviço.
 */
@Service
public class CsvBackupService {

    private static final Logger log = LoggerFactory.getLogger(CsvBackupService.class);

    private final Path backupDir;

    // Cada entrada: nome do arquivo -> (repositório, classe da entidade)
    private final Map<String, Repo> tabelas = new LinkedHashMap<>();

    private record Repo(CrudRepository<?, ?> repository, Class<?> tipo) {}

    public CsvBackupService(FichaTecnicaRepository fichaRepo,
                            MarcaRepository marcaRepo,
                            ColecaoRepository colecaoRepo,
                            InsumoRepository insumoRepo,
                            FornecedorRepository fornecedorRepo,
                            CategoriaRepository categoriaRepo,
                            BandeiraNovidadeRepository bandeiraRepo,
                            QuadroPlanejamentoRepository planejamentoRepo,
                            CadastroPrevioRepository cadastroPrevioRepo,
                            @Value("${app.backup.dir}") String backupPath) {
        this.backupDir = Paths.get(backupPath).toAbsolutePath();
        try {
            Files.createDirectories(this.backupDir);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de backup: " + this.backupDir, e);
        }

        tabelas.put("fichas_tecnicas",     new Repo(fichaRepo,        FichaTecnica.class));
        tabelas.put("marcas",              new Repo(marcaRepo,        Marca.class));
        tabelas.put("colecoes",            new Repo(colecaoRepo,      Colecao.class));
        tabelas.put("insumos",             new Repo(insumoRepo,       Insumo.class));
        tabelas.put("fornecedores",        new Repo(fornecedorRepo,   Fornecedor.class));
        tabelas.put("categorias",          new Repo(categoriaRepo,    Categoria.class));
        tabelas.put("bandeiras_novidade",  new Repo(bandeiraRepo,     BandeiraNovidade.class));
        tabelas.put("quadro_planejamento", new Repo(planejamentoRepo, QuadroPlanejamento.class));
        tabelas.put("cadastro_previo",     new Repo(cadastroPrevioRepo, CadastroPrevio.class));
    }

    /** Backup ao iniciar a aplicação, para já existir uma cópia atualizada. */
    @PostConstruct
    public void backupAoIniciar() {
        backupTodos();
    }

    /** Backup automático periódico (padrão: a cada 5 minutos). */
    @Scheduled(fixedDelayString = "${app.backup.intervalo-ms:300000}")
    public void backupAgendado() {
        backupTodos();
    }

    /** Exporta todas as tabelas. Sincronizado para evitar escrita concorrente nos arquivos. */
    public synchronized int backupTodos() {
        int totalLinhas = 0;
        for (Map.Entry<String, Repo> e : tabelas.entrySet()) {
            try {
                totalLinhas += exportar(e.getKey(), e.getValue());
            } catch (Exception ex) {
                log.error("Falha ao gerar backup CSV de '{}': {}", e.getKey(), ex.getMessage(), ex);
            }
        }
        log.info("Backup CSV concluído em {} ({} registros no total).", backupDir, totalLinhas);
        return totalLinhas;
    }

    private int exportar(String nomeArquivo, Repo repo) throws IOException {
        List<Field> campos = camposExportaveis(repo.tipo());
        List<Method> getters = new ArrayList<>();
        List<String> cabecalhos = new ArrayList<>();
        for (Field f : campos) {
            Method getter = getterDe(repo.tipo(), f);
            if (getter != null) {
                getters.add(getter);
                cabecalhos.add(f.getName());
            }
        }

        Path arquivo = backupDir.resolve(nomeArquivo + ".csv");
        int linhas = 0;
        try (BufferedWriter w = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            w.write(0xFEFF); // BOM: faz o Excel abrir acentos corretamente (UTF-8 com BOM)
            w.write(String.join(",", cabecalhos.stream().map(this::escapar).toList()));
            w.write("\r\n");

            for (Object entidade : repo.repository().findAll()) {
                List<String> valores = new ArrayList<>(getters.size());
                for (Method g : getters) {
                    Object valor;
                    try {
                        valor = g.invoke(entidade);
                    } catch (ReflectiveOperationException ex) {
                        valor = null;
                    }
                    valores.add(escapar(valor));
                }
                w.write(String.join(",", valores));
                w.write("\r\n");
                linhas++;
            }
        }
        return linhas;
    }

    /** Campos de instância da entidade, na ordem de declaração (ignora estáticos/sintéticos). */
    private List<Field> camposExportaveis(Class<?> tipo) {
        List<Field> resultado = new ArrayList<>();
        for (Field f : tipo.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
            resultado.add(f);
        }
        return resultado;
    }

    /** Localiza o getter público (getX / isX) correspondente ao campo. */
    private Method getterDe(Class<?> tipo, Field campo) {
        String nome = campo.getName();
        String capitalizado = Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
        for (String prefixo : new String[]{"get", "is"}) {
            try {
                return tipo.getMethod(prefixo + capitalizado);
            } catch (NoSuchMethodException ignored) {
                // tenta o próximo prefixo
            }
        }
        return null;
    }

    /** Escapa um valor conforme RFC 4180 (aspas quando há vírgula, aspas ou quebra de linha). */
    private String escapar(Object valor) {
        if (valor == null) return "";
        String s = String.valueOf(valor);
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    public Path getBackupDir() {
        return backupDir;
    }
}
