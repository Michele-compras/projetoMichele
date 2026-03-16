package com.example.projeto.repository;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.model.TipoItem;
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
         + "(:dataFim IS NULL OR f.dataColocacaoPedido <= :dataFim) "
         + "ORDER BY f.id DESC")
    List<FichaTecnica> buscarComFiltros(
            @Param("colecao") String colecao,
            @Param("tipo") TipoItem tipo,
            @Param("statusPedido") StatusPedido statusPedido,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    @Query("SELECT f.colecao, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao ORDER BY f.colecao ASC")
    List<Object[]> countByColecao();

    @Query("SELECT f.colecao, f.statusAmostraCor, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.statusAmostraCor ORDER BY f.colecao ASC")
    List<Object[]> countByColecaoAndStatusAmostraCor();

    @Query("SELECT f.colecao, f.statusAmostraProducao, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.statusAmostraProducao ORDER BY f.colecao ASC")
    List<Object[]> countByColecaoAndStatusAmostraProducao();

    @Query("SELECT f.colecao, f.tipo, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.tipo ORDER BY f.colecao ASC")
    List<Object[]> countByColecaoAndTipo();

    @Query("SELECT f.colecao, f.statusPedido, COUNT(f) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.statusPedido ORDER BY f.colecao ASC")
    List<Object[]> countByColecaoAndStatusPedido();

    long countByTipo(TipoItem tipo);

    long countByStatusPedido(StatusPedido statusPedido);

    long countByStatusAmostraCor(com.example.projeto.model.StatusAmostra status);

    long countByStatusAmostraProducao(com.example.projeto.model.StatusAmostra status);

    @Query("SELECT f.marca, COUNT(f) FROM FichaTecnica f WHERE f.marca IS NOT NULL GROUP BY f.marca ORDER BY COUNT(f) DESC")
    List<Object[]> countByMarca();

    @Query("SELECT f.fornecedor, f.colecao, COUNT(f) FROM FichaTecnica f WHERE f.fornecedor IS NOT NULL AND f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.fornecedor, f.colecao ORDER BY f.fornecedor ASC, f.colecao ASC")
    List<Object[]> countByFornecedorAndColecao();

    @Query("SELECT f FROM FichaTecnica f WHERE f.marca IS NOT NULL AND f.dataColocacaoPedido IS NOT NULL AND f.dataAprovacaoAmostraCor IS NOT NULL")
    List<FichaTecnica> findComLeadtimeAprovacaoCor();

    @Query("SELECT f.colecao, f.tipo, SUM(f.minimo), SUM(f.quantidadeComprada) FROM FichaTecnica f WHERE f.colecao IS NOT NULL AND f.colecao <> '' GROUP BY f.colecao, f.tipo ORDER BY f.colecao ASC")
    List<Object[]> sumQuantidadeByColecaoAndTipo();

    @Query("SELECT f.marcaQueComprou, f.tipo, SUM(f.minimo), SUM(f.quantidadeComprada) FROM FichaTecnica f WHERE f.marcaQueComprou IS NOT NULL GROUP BY f.marcaQueComprou, f.tipo ORDER BY f.marcaQueComprou ASC")
    List<Object[]> sumQuantidadeByMarcaAndTipo();
}
