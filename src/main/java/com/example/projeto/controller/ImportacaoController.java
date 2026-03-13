package com.example.projeto.controller;

import com.example.projeto.service.ImportacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/importar")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @GetMapping
    public String formulario() {
        return "importar";
    }

    @PostMapping
    public String importar(@RequestParam("arquivo") MultipartFile arquivo,
                           RedirectAttributes redirectAttributes) {
        if (arquivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Selecione um arquivo Excel (.xlsx)");
            return "redirect:/importar";
        }

        try {
            int quantidade = importacaoService.importarExcel(arquivo);
            redirectAttributes.addFlashAttribute("mensagem",
                    quantidade + " ficha(s) técnica(s) importada(s) com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro",
                    "Erro ao importar: " + e.getMessage());
        }

        return "redirect:/importar";
    }
}
