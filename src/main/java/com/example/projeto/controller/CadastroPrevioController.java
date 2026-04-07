package com.example.projeto.controller;

import com.example.projeto.model.Categoria;
import com.example.projeto.model.Colecao;
import com.example.projeto.model.Fornecedor;
import com.example.projeto.model.Insumo;
import com.example.projeto.model.Marca;
import com.example.projeto.repository.CategoriaRepository;
import com.example.projeto.repository.ColecaoRepository;
import com.example.projeto.repository.FornecedorRepository;
import com.example.projeto.repository.InsumoRepository;
import com.example.projeto.repository.MarcaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cadastro-previo")
public class CadastroPrevioController {

    private final MarcaRepository marcaRepo;
    private final ColecaoRepository colecaoRepo;
    private final InsumoRepository insumoRepo;
    private final CategoriaRepository categoriaRepo;
    private final FornecedorRepository fornecedorRepo;

    public CadastroPrevioController(MarcaRepository marcaRepo, ColecaoRepository colecaoRepo,
                                    InsumoRepository insumoRepo, CategoriaRepository categoriaRepo,
                                    FornecedorRepository fornecedorRepo) {
        this.marcaRepo = marcaRepo;
        this.colecaoRepo = colecaoRepo;
        this.insumoRepo = insumoRepo;
        this.categoriaRepo = categoriaRepo;
        this.fornecedorRepo = fornecedorRepo;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("marcas", marcaRepo.findAll());
        model.addAttribute("colecoes", colecaoRepo.findAll());
        model.addAttribute("insumos", insumoRepo.findAll());
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("fornecedores", fornecedorRepo.findAll());
        model.addAttribute("novaMarca", new Marca());
        model.addAttribute("novaColecao", new Colecao());
        model.addAttribute("novoInsumo", new Insumo());
        model.addAttribute("novaCategoria", new Categoria());
        model.addAttribute("novoFornecedor", new Fornecedor());
        return "cadastro-previo/lista";
    }

    // Marca
    @PostMapping("/marca/salvar")
    public String salvarMarca(@ModelAttribute("novaMarca") Marca marca, RedirectAttributes ra) {
        if (marca.getNome() != null && !marca.getNome().isBlank()) {
            marcaRepo.save(marca);
            ra.addFlashAttribute("sucesso", "Marca salva com sucesso.");
        }
        return "redirect:/cadastro-previo";
    }

    @PostMapping("/marca/excluir/{id}")
    public String excluirMarca(@PathVariable Long id, RedirectAttributes ra) {
        marcaRepo.deleteById(id);
        ra.addFlashAttribute("sucesso", "Marca excluída.");
        return "redirect:/cadastro-previo";
    }

    // Coleção
    @PostMapping("/colecao/salvar")
    public String salvarColecao(@ModelAttribute("novaColecao") Colecao colecao, RedirectAttributes ra) {
        if (colecao.getNome() != null && !colecao.getNome().isBlank()) {
            colecaoRepo.save(colecao);
            ra.addFlashAttribute("sucesso", "Coleção salva com sucesso.");
        }
        return "redirect:/cadastro-previo";
    }

    @PostMapping("/colecao/excluir/{id}")
    public String excluirColecao(@PathVariable Long id, RedirectAttributes ra) {
        colecaoRepo.deleteById(id);
        ra.addFlashAttribute("sucesso", "Coleção excluída.");
        return "redirect:/cadastro-previo";
    }

    // Insumo
    @PostMapping("/insumo/salvar")
    public String salvarInsumo(@ModelAttribute("novoInsumo") Insumo insumo, RedirectAttributes ra) {
        if (insumo.getNome() != null && !insumo.getNome().isBlank()) {
            insumoRepo.save(insumo);
            ra.addFlashAttribute("sucesso", "Insumo salvo com sucesso.");
        }
        return "redirect:/cadastro-previo";
    }

    @PostMapping("/insumo/excluir/{id}")
    public String excluirInsumo(@PathVariable Long id, RedirectAttributes ra) {
        insumoRepo.deleteById(id);
        ra.addFlashAttribute("sucesso", "Insumo excluído.");
        return "redirect:/cadastro-previo";
    }

    // Categoria
    @PostMapping("/categoria/salvar")
    public String salvarCategoria(@ModelAttribute("novaCategoria") Categoria categoria, RedirectAttributes ra) {
        if (categoria.getNome() != null && !categoria.getNome().isBlank()) {
            categoriaRepo.save(categoria);
            ra.addFlashAttribute("sucesso", "Categoria salva com sucesso.");
        }
        return "redirect:/cadastro-previo";
    }

    @PostMapping("/categoria/excluir/{id}")
    public String excluirCategoria(@PathVariable Long id, RedirectAttributes ra) {
        categoriaRepo.deleteById(id);
        ra.addFlashAttribute("sucesso", "Categoria excluída.");
        return "redirect:/cadastro-previo";
    }

    // Fornecedor
    @PostMapping("/fornecedor/salvar")
    public String salvarFornecedor(@ModelAttribute("novoFornecedor") Fornecedor fornecedor, RedirectAttributes ra) {
        if (fornecedor.getNome() != null && !fornecedor.getNome().isBlank()) {
            fornecedorRepo.save(fornecedor);
            ra.addFlashAttribute("sucesso", "Fornecedor salvo com sucesso.");
        }
        return "redirect:/cadastro-previo";
    }

    @PostMapping("/fornecedor/excluir/{id}")
    public String excluirFornecedor(@PathVariable Long id, RedirectAttributes ra) {
        fornecedorRepo.deleteById(id);
        ra.addFlashAttribute("sucesso", "Fornecedor excluído.");
        return "redirect:/cadastro-previo";
    }
}
