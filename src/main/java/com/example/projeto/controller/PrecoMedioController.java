package com.example.projeto.controller;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.repository.FichaTecnicaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/preco-medio")
public class PrecoMedioController {

    private final FichaTecnicaRepository repository;

    public PrecoMedioController(FichaTecnicaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String precoMedio(
            @RequestParam(required = false) String colecao,
            @RequestParam(required = false) String fornecedor,
            @RequestParam(required = false) String marca,
            Model model) {

        List<FichaTecnica> todas = repository.findAll();

        List<String> colecoes = todas.stream()
                .map(FichaTecnica::getColecao)
                .filter(c -> c != null && !c.isBlank())
                .distinct().sorted().collect(Collectors.toList());

        List<String> fornecedores = todas.stream()
                .map(FichaTecnica::getFornecedor)
                .filter(f -> f != null && !f.isBlank())
                .distinct().sorted().collect(Collectors.toList());

        List<String> marcas = todas.stream()
                .map(FichaTecnica::getMarca)
                .filter(m -> m != null && !m.isBlank())
                .distinct().sorted().collect(Collectors.toList());

        List<FichaTecnica> fichas = todas.stream()
                .filter(f -> colecao == null || colecao.isBlank()
                        || colecao.equalsIgnoreCase(f.getColecao()))
                .filter(f -> fornecedor == null || fornecedor.isBlank()
                        || fornecedor.equalsIgnoreCase(f.getFornecedor()))
                .filter(f -> marca == null || marca.isBlank()
                        || marca.equalsIgnoreCase(f.getMarca()))
                .filter(f -> f.getPrecoUsd() != null || f.getPrecoReais() != null)
                .collect(Collectors.toList());

        // Agrupar por categoria + marca
        Map<String, Map<String, List<FichaTecnica>>> porCategoriaMarca = fichas.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getCategoriaProduto() != null && !f.getCategoriaProduto().isBlank()
                                ? f.getCategoriaProduto() : "(Sem categoria)",
                        Collectors.groupingBy(
                                f -> f.getMarca() != null && !f.getMarca().isBlank()
                                        ? f.getMarca() : "(Sem marca)")));

        List<Map<String, Object>> linhas = new ArrayList<>();
        for (String cat : porCategoriaMarca.keySet().stream().sorted().collect(Collectors.toList())) {
            Map<String, List<FichaTecnica>> porMarca = porCategoriaMarca.get(cat);
            for (String marc : porMarca.keySet().stream().sorted().collect(Collectors.toList())) {
                List<FichaTecnica> grupo = porMarca.get(marc);

                OptionalDouble mediaUsd = grupo.stream()
                        .filter(f -> f.getPrecoUsd() != null)
                        .mapToDouble(FichaTecnica::getPrecoUsd).average();

                OptionalDouble mediaReais = grupo.stream()
                        .filter(f -> f.getPrecoReais() != null)
                        .mapToDouble(FichaTecnica::getPrecoReais).average();

                Map<String, Object> linha = new LinkedHashMap<>();
                linha.put("categoria",  cat);
                linha.put("marca",      marc);
                linha.put("qtd",        grupo.size());
                linha.put("mediaUsd",   mediaUsd.isPresent()   ? mediaUsd.getAsDouble()   : null);
                linha.put("mediaReais", mediaReais.isPresent() ? mediaReais.getAsDouble() : null);
                linhas.add(linha);
            }
        }

        OptionalDouble totalMediaUsd = fichas.stream()
                .filter(f -> f.getPrecoUsd() != null)
                .mapToDouble(FichaTecnica::getPrecoUsd).average();
        OptionalDouble totalMediaReais = fichas.stream()
                .filter(f -> f.getPrecoReais() != null)
                .mapToDouble(FichaTecnica::getPrecoReais).average();

        model.addAttribute("colecoes",       colecoes);
        model.addAttribute("fornecedores",   fornecedores);
        model.addAttribute("marcas",         marcas);
        model.addAttribute("colecaoSel",     colecao);
        model.addAttribute("fornecedorSel",  fornecedor);
        model.addAttribute("marcaSel",       marca);
        model.addAttribute("linhas",         linhas);
        model.addAttribute("totalFichas",    fichas.size());
        model.addAttribute("totalMediaUsd",  totalMediaUsd.isPresent()   ? totalMediaUsd.getAsDouble()   : null);
        model.addAttribute("totalMediaReais",totalMediaReais.isPresent() ? totalMediaReais.getAsDouble() : null);

        return "preco-medio";
    }
}
