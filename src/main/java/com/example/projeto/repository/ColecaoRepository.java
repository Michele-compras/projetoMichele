package com.example.projeto.repository;

import com.example.projeto.model.Colecao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColecaoRepository extends JpaRepository<Colecao, Long> {
}
