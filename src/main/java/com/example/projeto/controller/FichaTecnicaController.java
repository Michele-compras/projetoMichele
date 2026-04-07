package com.example.projeto.controller;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusAmostra;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.repository.CategoriaRepository;
import com.example.projeto.repository.ColecaoRepository;
import com.example.projeto.repository.FornecedorRepository;
import com.example.projeto.repository.InsumoRepository;
import com.example.projeto.repository.MarcaRepository;
import com.example.projeto.service.FichaTecnicaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/fichas")
public class FichaTecnicaController {

    private final FichaTecnicaService service;
    private final MarcaRepository marcaRepo;
    private final ColecaoRepository colecaoRepo;
    private final InsumoRepository insumoRepo;
    private final FornecedorRepository fornecedorRepo;
    private final CategoriaRepository categoriaRepo;

    public FichaTecnicaController(FichaTecnicaService service, MarcaRepository marcaRepo,
                                   ColecaoRepository colecaoRepo, InsumoRepository insumoRepo,
                                   FornecedorRepository fornecedorRepo, CategoriaRepository categoriaRepo) {
        this.service = service;
        this.marcaRepo = marcaRepo;
        this.colecaoRepo = colecaoRepo;
        this.insumoRepo = insumoRepo;
        this.fornecedorRepo = fornecedorRepo;
        this.categoriaRepo = categoriaRepo;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String colecao,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) StatusPedido statusPedido,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            @RequestParam(required = false) String duimpDi,
            @RequestParam(required = false) String contratoCambio,
            Model model) {

        List<FichaTecnica> fichas;
        boolean temFiltro = colecao != null || tipo != null
                || statusPedido != null || dataInicio != null || dataFim != null
                || (duimpDi != null && !duimpDi.isBlank())
                || (contratoCambio != null && !contratoCambio.isBlank());

        if (temFiltro) {
            fichas = service.buscarComFiltros(colecao, tipo, statusPedido, dataInicio, dataFim, duimpDi, contratoCambio);
        } else {
            fichas = service.listarTodas();
        }

        model.addAttribute("fichas", fichas);
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("colecoesCadastradas", colecaoRepo.findAll());
        model.addAttribute("colecaoFiltro", colecao);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("statusPedidoSelecionado", statusPedido);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("duimpDiFiltro", duimpDi);
        model.addAttribute("contratoCambioFiltro", contratoCambio);
        model.addAttribute("qtdPorColecao", service.qtdPorColecao());
        model.addAttribute("qtdTipoPorColecao", service.qtdTipoPorColecao());
        return "fichas/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        addFormAttributes(model, new FichaTecnica());
        return "fichas/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("ficha") FichaTecnica ficha,
                         BindingResult bindingResult,
                         @RequestParam(value = "foto", required = false) MultipartFile foto,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, ficha);
            model.addAttribute("erro", "Verifique os campos obrigatórios.");
            return "fichas/formulario";
        }
        try {
            service.salvar(ficha, foto);
            redirectAttributes.addFlashAttribute("mensagem", "Ficha técnica salva com sucesso!");
            return "redirect:/fichas";
        } catch (Exception e) {
            addFormAttributes(model, ficha);
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            return "fichas/formulario";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        addFormAttributes(model, service.buscarPorId(id));
        return "fichas/formulario";
    }

    @PostMapping("/foto/{id}")
    public String uploadFoto(@PathVariable Long id,
                             @RequestParam("foto") MultipartFile foto,
                             RedirectAttributes redirectAttributes) {
        try {
            FichaTecnica ficha = service.buscarPorId(id);
            service.salvar(ficha, foto);
            redirectAttributes.addFlashAttribute("mensagem", "Foto atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar foto: " + e.getMessage());
        }
        return "redirect:/fichas/editar/" + id;
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.excluir(id);
        redirectAttributes.addFlashAttribute("mensagem", "Ficha técnica excluída com sucesso!");
        return "redirect:/fichas";
    }

    @GetMapping("/visualizar/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        model.addAttribute("ficha", service.buscarPorId(id));
        return "fichas/visualizar";
    }

    private void addFormAttributes(Model model, FichaTecnica ficha) {
        model.addAttribute("ficha", ficha);
        model.addAttribute("statusList", new StatusAmostra[]{StatusAmostra.PENDENTE, StatusAmostra.APROVADO});
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("marcasCadastradas", marcaRepo.findAll());
        model.addAttribute("colecoesCadastradas", colecaoRepo.findAll());
        model.addAttribute("fornecedoresCadastrados", fornecedorRepo.findAll());
        model.addAttribute("categoriasCadastradas", categoriaRepo.findAll());
    }
}
