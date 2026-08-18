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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
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
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String fornecedor,
            Model model) {

        List<FichaTecnica> fichas;
        boolean temFiltro = colecao != null || tipo != null
                || statusPedido != null || dataInicio != null || dataFim != null
                || (duimpDi != null && !duimpDi.isBlank())
                || (contratoCambio != null && !contratoCambio.isBlank())
                || (codigo != null && !codigo.isBlank())
                || (fornecedor != null && !fornecedor.isBlank());

        if (temFiltro) {
            fichas = service.buscarComFiltros(colecao, tipo, statusPedido, dataInicio, dataFim, duimpDi, contratoCambio, codigo, null, fornecedor);
        } else {
            fichas = service.listarTodas();
        }

        model.addAttribute("fichas", fichas);
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("colecoesCadastradas", colecaoRepo.findAll());
        model.addAttribute("fornecedoresCadastrados", fornecedorRepo.findAll());
        model.addAttribute("colecaoFiltro", colecao);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("statusPedidoSelecionado", statusPedido);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("duimpDiFiltro", duimpDi);
        model.addAttribute("contratoCambioFiltro", contratoCambio);
        model.addAttribute("codigoFiltro", codigo);
        model.addAttribute("fornecedorFiltro", fornecedor);
        model.addAttribute("qtdPorColecao", service.qtdPorColecao());
        model.addAttribute("qtdTipoPorColecao", service.qtdTipoPorColecao());
        model.addAttribute("qtdPorColecaoETipo", service.qtdPorColecaoETipoPlano());
        model.addAttribute("tiposColunas", service.tiposUsados());
        return "fichas/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        addFormAttributes(model, new FichaTecnica());
        addFiltroAttributes(model, null, null, null, null, null, null, null);
        return "fichas/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("ficha") FichaTecnica ficha,
                         BindingResult bindingResult,
                         @RequestParam(value = "foto", required = false) MultipartFile foto,
                         @RequestParam(required = false) String fColecao,
                         @RequestParam(required = false) String fTipo,
                         @RequestParam(required = false) String fStatus,
                         @RequestParam(required = false) String fCodigo,
                         @RequestParam(required = false) String fDuimp,
                         @RequestParam(required = false) String fContrato,
                         @RequestParam(required = false) String fFornecedor,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, ficha);
            addFiltroAttributes(model, fColecao, fTipo, fStatus, fCodigo, fDuimp, fContrato, fFornecedor);
            model.addAttribute("erro", "Verifique os campos obrigatórios.");
            return "fichas/formulario";
        }
        try {
            FichaTecnica salva = service.salvar(ficha, foto);
            redirectAttributes.addFlashAttribute("mensagem", "Ficha técnica salva com sucesso!");
            // Volta para a lista mantendo o filtro selecionado e posicionando na ficha editada.
            return "redirect:/fichas"
                    + montarQueryFiltros(fColecao, fTipo, fStatus, fCodigo, fDuimp, fContrato, fFornecedor)
                    + "#ficha-" + salva.getId();
        } catch (Exception e) {
            addFormAttributes(model, ficha);
            addFiltroAttributes(model, fColecao, fTipo, fStatus, fCodigo, fDuimp, fContrato, fFornecedor);
            model.addAttribute("erro", "Erro ao salvar: " + e.getMessage());
            return "fichas/formulario";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id,
                         @RequestParam(required = false) String fColecao,
                         @RequestParam(required = false) String fTipo,
                         @RequestParam(required = false) String fStatus,
                         @RequestParam(required = false) String fCodigo,
                         @RequestParam(required = false) String fDuimp,
                         @RequestParam(required = false) String fContrato,
                         @RequestParam(required = false) String fFornecedor,
                         Model model) {
        addFormAttributes(model, service.buscarPorId(id));
        addFiltroAttributes(model, fColecao, fTipo, fStatus, fCodigo, fDuimp, fContrato, fFornecedor);
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
    public String visualizar(@PathVariable Long id,
                            @RequestParam(required = false) String fColecao,
                            @RequestParam(required = false) String fTipo,
                            @RequestParam(required = false) String fStatus,
                            @RequestParam(required = false) String fCodigo,
                            @RequestParam(required = false) String fDuimp,
                            @RequestParam(required = false) String fContrato,
                            @RequestParam(required = false) String fFornecedor,
                            Model model) {
        model.addAttribute("ficha", service.buscarPorId(id));
        addFiltroAttributes(model, fColecao, fTipo, fStatus, fCodigo, fDuimp, fContrato, fFornecedor);
        return "fichas/visualizar";
    }

    private void addFormAttributes(Model model, FichaTecnica ficha) {
        model.addAttribute("ficha", ficha);
        model.addAttribute("statusList", new StatusAmostra[]{StatusAmostra.PENDENTE, StatusAmostra.EM_ANALISE, StatusAmostra.APROVADO, StatusAmostra.REPROVADO, StatusAmostra.CANCELADO});
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("marcasCadastradas", marcaRepo.findAll());
        model.addAttribute("colecoesCadastradas", colecaoRepo.findAll());
        model.addAttribute("fornecedoresCadastrados", fornecedorRepo.findAll());
        model.addAttribute("categoriasCadastradas", categoriaRepo.findAll());
    }

    /** Mantém os filtros da listagem disponíveis no formulário (campos ocultos e link Cancelar). */
    private void addFiltroAttributes(Model model, String fColecao, String fTipo, String fStatus,
                                     String fCodigo, String fDuimp, String fContrato,
                                     String fFornecedor) {
        model.addAttribute("fColecao", fColecao);
        model.addAttribute("fTipo", fTipo);
        model.addAttribute("fStatus", fStatus);
        model.addAttribute("fCodigo", fCodigo);
        model.addAttribute("fDuimp", fDuimp);
        model.addAttribute("fContrato", fContrato);
        model.addAttribute("fFornecedor", fFornecedor);
        // Query string pronta de /fichas (sem os filtros vazios), usada no link "Cancelar".
        model.addAttribute("filtrosQuery", montarQueryFiltros(fColecao, fTipo, fStatus, fCodigo, fDuimp, fContrato, fFornecedor));
    }

    /** Monta a query string de /fichas a partir dos filtros, ignorando os vazios. */
    private String montarQueryFiltros(String fColecao, String fTipo, String fStatus,
                                      String fCodigo, String fDuimp, String fContrato,
                                      String fFornecedor) {
        List<String> partes = new ArrayList<>();
        adicionarFiltro(partes, "colecao", fColecao);
        adicionarFiltro(partes, "tipo", fTipo);
        adicionarFiltro(partes, "statusPedido", fStatus);
        adicionarFiltro(partes, "codigo", fCodigo);
        adicionarFiltro(partes, "duimpDi", fDuimp);
        adicionarFiltro(partes, "contratoCambio", fContrato);
        adicionarFiltro(partes, "fornecedor", fFornecedor);
        return partes.isEmpty() ? "" : "?" + String.join("&", partes);
    }

    private void adicionarFiltro(List<String> partes, String nome, String valor) {
        if (valor != null && !valor.isBlank()) {
            partes.add(nome + "=" + URLEncoder.encode(valor, StandardCharsets.UTF_8));
        }
    }
}
