package com.quesssystems.sistemato.beans.pendencia;

import com.quesssystems.sistemato.beans.automacao.AutomacaoService;
import com.quesssystems.sistemato.beans.usuario.Usuario;
import com.quesssystems.sistemato.beans.usuario.UsuarioService;
import com.quesssystems.sistemato.exceptions.AutomacaoNaoEncontradaException;
import com.quesssystems.sistemato.exceptions.UsuarioNaoEncontradoException;
import com.quesssystems.sistemato.util.FileUtil;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PendenciaController {
    private final PendenciaService pendenciaService;
    private final UsuarioService usuarioService;
    private final AutomacaoService automacaoService;
    private final FileUtil fileUtil;

    public PendenciaController(PendenciaService pendenciaService, UsuarioService usuarioService, AutomacaoService automacaoService, FileUtil fileUtil) {
        this.pendenciaService = pendenciaService;
        this.usuarioService = usuarioService;
        this.automacaoService = automacaoService;
        this.fileUtil = fileUtil;
    }

    @GetMapping("/pendencias")
    public String listarPendencias(Model model, RedirectAttributes ra) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
            model.addAttribute("adm", usuarioLogado.isAdm());
            if (!usuarioLogado.isAdm()) {
                model.addAttribute("pagina", "automacoes");
                return "redirect:/automacoes/true?acessonegado";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        List<Pendencia> pendencias = new ArrayList<>();
        try {
            List<String> idsAutomacao = fileUtil.listarArquivos(fileUtil.getArquivosPendenciasPath());
            for (String idAutomacao : idsAutomacao) {
                pendencias.addAll(pendenciaService.listArquivos(automacaoService.get(Integer.valueOf(idAutomacao))));
            }
        }
        catch (IOException e) {
            ra.addFlashAttribute("mensagemErro", "Falha ao ler arquivos");
            model.addAttribute("pagina", "pendencias");
            return "redirect:/pendencias";
        }
        catch (AutomacaoNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            model.addAttribute("pagina", "automacoes");
            return "redirect:/automacoes/true";
        }

        model.addAttribute("pendencias", pendencias);
        model.addAttribute("pagina", "pendencias");

        return "pendencias";
    }

    @GetMapping("/pendencias/baixararquivo/{id}/{arquivo}")
    public ResponseEntity<Resource> baixarArquivo(@PathVariable("id") Integer id, @PathVariable("arquivo") String arquivo) {
        try {
            return fileUtil.download(fileUtil.getArquivosPendenciasPath() + id + "/" + arquivo, arquivo);
        }
        catch (IOException e) {
            return null;
        }
    }

    @GetMapping("/pendencias/excluirarquivo/{id}/{arquivo}")
    public String excluirArquivo(@PathVariable("id") Integer id, @PathVariable("arquivo") String arquivo, Model model, RedirectAttributes ra) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
            model.addAttribute("adm", usuarioLogado.isAdm());
            if (!usuarioLogado.isAdm()) {
                model.addAttribute("pagina", "automacoes");
                return "redirect:/automacoes/true?acessonegado";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            automacaoService.get(id);
            fileUtil.apagarArquivo(fileUtil.getArquivosPendenciasPath() + id + "/" + arquivo);
            ra.addFlashAttribute("mensagemSucesso", "O arquivo da pendência da automação foi apagado");
            model.addAttribute("pagina", "pendencias");
            return "redirect:/pendencias";
        }
        catch (IOException e) {
            ra.addFlashAttribute("mensagemErro", "Falha ao ler arquivos");
            model.addAttribute("pagina", "pendencias");
            return "redirect:/pendencias";
        }
        catch (AutomacaoNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            model.addAttribute("pagina", "automacoes");
            return "redirect:/automacoes/true";
        }
    }
}
