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
}
