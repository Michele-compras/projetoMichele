package com.example.projeto.repository;

import com.example.projeto.model.BandeiraNovidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BandeiraNovidadeRepository extends JpaRepository<BandeiraNovidade, Long> {
    Optional<BandeiraNovidade> findByColecaoAndFornecedorAndInsumo(String colecao, String fornecedor, String insumo);
}
