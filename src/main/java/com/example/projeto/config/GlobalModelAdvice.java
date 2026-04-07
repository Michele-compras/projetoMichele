package com.example.projeto.config;

import com.example.projeto.repository.InsumoRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final InsumoRepository insumoRepository;

    public GlobalModelAdvice(InsumoRepository insumoRepository) {
        this.insumoRepository = insumoRepository;
    }

    @ModelAttribute
    public void addInsumos(Model model) {
        model.addAttribute("insumosCadastrados", insumoRepository.findAll());
    }
}
