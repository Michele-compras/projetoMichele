package com.example.projeto.repository;

import com.example.projeto.model.QuadroPlanejamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuadroPlanejamentoRepository extends JpaRepository<QuadroPlanejamento, Long> {
    List<QuadroPlanejamento> findByColecaoOrderByTipoSolicitacaoAsc(String colecao);
    Optional<QuadroPlanejamento> findByColecaoAndTipoSolicitacao(String colecao, String tipoSolicitacao);
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT q.colecao FROM QuadroPlanejamento q WHERE q.colecao IS NOT NULL ORDER BY q.colecao ASC")
    List<String> findDistinctColecoes();

    @org.springframework.data.jpa.repository.Query(
        "SELECT q.colecao, " +
        "SUM(COALESCE(q.animeCotado,0) + COALESCE(q.momiCotado,0) + COALESCE(q.authoriaCotado,0) + COALESCE(q.bimbiCotado,0) + COALESCE(q.youcciecotado,0)), " +
        "SUM(COALESCE(q.animeAprovado,0) + COALESCE(q.momiAprovado,0) + COALESCE(q.authoriaAprovado,0) + COALESCE(q.bimbiAprovado,0) + COALESCE(q.youccieeAprovado,0)) " +
        "FROM QuadroPlanejamento q WHERE q.colecao IS NOT NULL GROUP BY q.colecao ORDER BY q.colecao DESC")
    List<Object[]> sumCotadoAprovadoByColecao();
}
