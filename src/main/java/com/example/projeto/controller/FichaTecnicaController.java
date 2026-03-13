package com.example.projeto.controller;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.StatusAmostra;
import com.example.projeto.model.StatusPedido;
import com.example.projeto.model.TipoItem;
import com.example.projeto.service.FichaTecnicaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/fichas")
public class FichaTecnicaController {

    private final FichaTecnicaService service;

    public FichaTecnicaController(FichaTecnicaService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String colecao,
            @RequestParam(required = false) TipoItem tipo,
            @RequestParam(required = false) StatusPedido statusPedido,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            Model model) {

        List<FichaTecnica> fichas;
        boolean temFiltro = colecao != null || tipo != null
                || statusPedido != null || dataInicio != null || dataFim != null;

        if (temFiltro) {
            fichas = service.buscarComFiltros(colecao, tipo, statusPedido, dataInicio, dataFim);
        } else {
            fichas = service.listarTodas();
        }

        model.addAttribute("fichas", fichas);
        model.addAttribute("tipos", TipoItem.values());
        model.addAttribute("statusPedidoList", StatusPedido.values());
        model.addAttribute("colecaoFiltro", colecao);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("statusPedidoSelecionado", statusPedido);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
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
    public String salvar(@ModelAttribute FichaTecnica ficha,
                         @RequestParam(value = "foto", required = false) MultipartFile foto,
                         RedirectAttributes redirectAttributes) {
        service.salvar(ficha, foto);
        redirectAttributes.addFlashAttribute("mensagem", "Ficha técnica salva com sucesso!");
        return "redirect:/fichas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        addFormAttributes(model, service.buscarPorId(id));
        return "fichas/formulario";
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
        model.addAttribute("tipos", TipoItem.values());
        model.addAttribute("statusList", StatusAmostra.values());
        model.addAttribute("statusPedidoList", StatusPedido.values());
    }
}
