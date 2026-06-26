package com.example.projeto.controller;

import com.example.projeto.service.FichaTecnicaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/resumo-amostra-cor")
public class ResumoAmostraCorController {

    private final FichaTecnicaService service;

    public ResumoAmostraCorController(FichaTecnicaService service) {
        this.service = service;
    }

    @GetMapping
    public String resumo(@RequestParam(required = false) String colecaoFiltro, Model model) {
        Map<String, Map<String, Map<String, Long>>> resumoCompleto = service.resumoStatusCorPorColecaoETipo();

        List<String> colecoes = List.copyOf(resumoCompleto.keySet());

        Map<String, Map<String, Map<String, Long>>> resumo = new LinkedHashMap<>();
        if (colecaoFiltro != null && !colecaoFiltro.isBlank()) {
            if (resumoCompleto.containsKey(colecaoFiltro)) {
                resumo.put(colecaoFiltro, resumoCompleto.get(colecaoFiltro));
            }
        } else {
            resumo.putAll(resumoCompleto);
        }

        Map<String, Map<String, Long>> subtotais = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Map<String, Long>>> colEntry : resumo.entrySet()) {
            Map<String, Long> sub = new LinkedHashMap<>();
            for (String s : List.of("PENDENTE", "EM_ANALISE", "APROVADO", "REPROVADO")) sub.put(s, 0L);
            for (Map<String, Long> statusMap : colEntry.getValue().values()) {
                for (String s : List.of("PENDENTE", "EM_ANALISE", "APROVADO", "REPROVADO")) {
                    sub.merge(s, statusMap.getOrDefault(s, 0L), Long::sum);
                }
            }
            subtotais.put(colEntry.getKey(), sub);
        }

        model.addAttribute("colecoes", colecoes);
        model.addAttribute("colecaoFiltro", colecaoFiltro != null ? colecaoFiltro : "");
        model.addAttribute("resumo", resumo);
        model.addAttribute("subtotais", subtotais);
        return "resumo-amostra-cor";
    }
}
