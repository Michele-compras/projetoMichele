package com.example.projeto.repository;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusAmostra;
import com.example.projeto.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FichaTecnicaRepository extends JpaRepository<FichaTecnica, Long> {

    @Query("SELECT f FROM FichaTecnica f WHERE "
         + "(:colecao IS NULL OR LOWER(f.colecao) LIKE LOWER(CONCAT('%', :colecao, '%'))) AND "
         + "(:tipo IS NULL OR f.tipo = :tipo) AND "
         + "(:statusPedido IS NULL OR f.statusPedido = :statusPedido) AND "
         + "(:dataInicio IS NULL OR f.dataColocacaoPedido >= :dataInicio) AND "
         + "(:dataFim IS NULL OR f.dataColocacaoPedido <= :dataFim) AND "
         + "(:duimpDi IS NULL OR LOWER(f.duimpDi) LIKE LOWER(CONCAT('%', :duimpDi, '%'))) AND "
         + "(:contratoCambio IS NULL OR LOWER(f.contratoCambioAdiant) LIKE LOWER(CONCAT('%', :contratoCambio, '%')) OR LOWER(f.contratoCambioPgtoFinal) LIKE LOWER(CONCAT('%', :contratoCambio, '%'))) "
         + "ORDER BY f.id DESC")
    List<FichaTecnica> buscarComFiltros(
            @Param("colecao") String colecao,
            @Param("tipo") String tipo,
            @Param("statusPedido") StatusPedido statusPedido,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("duimpDi") String duimpDi,
            @Param("contratoCambio") String contratoCambio);

    @Query("SELECT f.colecao, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao ORDER BY f.colecao ASC")
    List<Object[]> countByColecao();

    @Query("SELECT f.colecao, f.statusAmostraCor, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.statusAmostraCor ORDER BY f.colecao ASC")
    List<Object[]> countByColecaoAndStatusAmostraCor();

    @Query("SELECT f.colecao, f.tipo, f.statusAmostraCor, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' AND f.tipo IS NOT NULL GROUP BY f.colecao, f.tipo, f.statusAmostraCor ORDER BY f.colecao ASC, f.tipo ASC")
    List<Object[]> countByColecaoAndTipoAndStatusAmostraCor();

    @Query("SELECT f.colecao, f.tipo, f.statusAmostraProducao, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' AND f.tipo IS NOT NULL GROUP BY f.colecao, f.tipo, f.statusAmostraProducao ORDER BY f.colecao ASC, f.tipo ASC")
    List<Object[]> countByColecaoAndTipoAndStatusAmostraProducao();

    @Query("SELECT f.colecao, f.statusAmostraProducao, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.statusAmostraProducao ORDER BY f.colecao ASC")
    List<Object[]> countByColecaoAndStatusAmostraProducao();

    @Query("SELECT f.colecao, f.tipo, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.tipo ORDER BY f.colecao ASC")
    List<Object[]> countByColecaoAndTipo();

    @Query("SELECT f.colecao, f.statusPedido, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.statusPedido ORDER BY f.colecao ASC")
    List<Object[]> countByColecaoAndStatusPedido();

    long countByTipo(String tipo);

    long countByStatusPedido(StatusPedido statusPedido);

    long countByStatusAmostraCor(StatusAmostra status);

    long countByStatusAmostraProducao(StatusAmostra status);

    @Query("SELECT f.tipo, COUNT(f) FROM FichaTecnica f WHERE f.tipo IS NOT NULL GROUP BY f.tipo ORDER BY COUNT(f) DESC")
    List<Object[]> countByTipoAll();

    @Query("SELECT f.marca, COUNT(f) FROM FichaTecnica f WHERE f.marca IS NOT NULL GROUP BY f.marca ORDER BY COUNT(f) DESC")
    List<Object[]> countByMarca();

    @Query("SELECT f.fornecedor, f.colecao, COUNT(f) FROM FichaTecnica f WHERE f.fornecedor IS NOT NULL AND f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.fornecedor, f.colecao ORDER BY f.fornecedor ASC, f.colecao ASC")
    List<Object[]> countByFornecedorAndColecao();

    @Query("SELECT f FROM FichaTecnica f WHERE f.marca IS NOT NULL AND f.dataColocacaoPedido IS NOT NULL AND f.dataAprovacaoAmostraCor IS NOT NULL")
    List<FichaTecnica> findComLeadtimeAprovacaoCor();

    @Query("SELECT f FROM FichaTecnica f WHERE f.marca IS NOT NULL AND f.dataColocacaoPedido IS NOT NULL AND f.dataAprovacaoAmostraProducao IS NOT NULL")
    List<FichaTecnica> findComLeadtimeAprovacaoProducao();

    @Query("SELECT f.colecao, f.tipo, SUM(f.minimo), SUM(f.quantidadeComprada) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.tipo ORDER BY f.colecao ASC")
    List<Object[]> sumQuantidadeByColecaoAndTipo();

    @Query("SELECT f.colecao, COUNT(f), COUNT(f.quantidadeComprada) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao ORDER BY f.colecao ASC")
    List<Object[]> sumQuantidadeByColecao();

    @Query("SELECT f.marcaQueComprou, f.tipo, SUM(f.minimo), SUM(f.quantidadeComprada) FROM FichaTecnica f WHERE f.marcaQueComprou IS NOT NULL GROUP BY f.marcaQueComprou, f.tipo ORDER BY f.marcaQueComprou ASC")
    List<Object[]> sumQuantidadeByMarcaAndTipo();

    @Query("SELECT f.marca, f.colecao, COUNT(f), COUNT(f.quantidadeComprada) FROM FichaTecnica f WHERE f.marca IS NOT NULL AND f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.marca, f.colecao ORDER BY f.colecao DESC, f.marca ASC")
    List<Object[]> countOrcadoCompradoByMarca();
}
