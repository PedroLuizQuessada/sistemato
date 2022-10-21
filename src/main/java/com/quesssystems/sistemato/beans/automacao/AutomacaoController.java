package com.quesssystems.sistemato.beans.automacao;

import com.quesssystems.sistemato.beans.execucao.Execucao;
import com.quesssystems.sistemato.beans.execucao.ExecucaoService;
import com.quesssystems.sistemato.beans.usuario.Usuario;
import com.quesssystems.sistemato.beans.usuario.UsuarioService;
import com.quesssystems.sistemato.exceptions.AutomacaoNaoEncontradaException;
import com.quesssystems.sistemato.exceptions.UsuarioNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class AutomacaoController {

    @Value("${server.link}")
    private String servidorLink;

    @Value("${server.servlet.context-path}")
    private String servidorContextPath;

    private final AutomacaoService automacaoService;
    private final UsuarioService usuarioService;

    private final ExecucaoService execucaoService;

    public AutomacaoController(AutomacaoService automacaoService, UsuarioService usuarioService, ExecucaoService execucaoService) {
        this.automacaoService = automacaoService;
        this.usuarioService = usuarioService;
        this.execucaoService = execucaoService;
    }

    @GetMapping("/automacoes/{ativo}")
    public String listarAutomacoes(@PathVariable("ativo") boolean ativo, Model model) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        model.addAttribute("adm", usuarioLogado.isAdm());
        model.addAttribute("automacoes", automacaoService.listAll(ativo));
        model.addAttribute("ativos", ativo);
        model.addAttribute("link", servidorLink);
        model.addAttribute("contextPath", servidorContextPath);

        return "automacoes";
    }

    @GetMapping("/automacoes/consultar/{id}")
    public String consultarAutomacao(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
        try {
            usuarioService.getUsuarioLogado();
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            Automacao automacao = automacaoService.get(id);

            model.addAttribute("tituloPagina", String.format("Automação %d", automacao.getId()));
            model.addAttribute("automacao", automacao);
            List<Execucao> execucoes = execucaoService.listAll(automacao);
            Collections.reverse(execucoes);
            model.addAttribute("execucoes", execucoes);
            return "automacao";
        }
        catch (AutomacaoNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/automacoes/true";
        }
    }

    @GetMapping("/automacoes/ativotoggle/{id}")
    public String ativoToggleAutomacao(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            usuarioService.getUsuarioLogado();
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            boolean ativada = automacaoService.ativoToggle(id);
            String mensagem = "A automação %d foi ativada";
            if (!ativada) {
                mensagem = "A automação %d foi inativada";
            }
            ra.addFlashAttribute("mensagemSucesso", String.format(mensagem, id));
        }
        catch (AutomacaoNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/automacoes/true";
    }

    @GetMapping("/automacoes/excluir/{id}")
    public String excluirAutomacao(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            if (!usuarioService.getUsuarioLogado().isAdm()) {
                return "redirect:/automacoes/true?acessonegado";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            automacaoService.delete(id);
            ra.addFlashAttribute("mensagemSucesso", String.format("A automação %d foi deletada", id));
        }
        catch (AutomacaoNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/automacoes/true";
    }

    @GetMapping("/automacoes/novo")
    public String adicionarAutomacao(Model model) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        if (usuarioLogado.isAdm()) {
            model.addAttribute("automacao", new Automacao());
            model.addAttribute("tituloPagina", "Adicionar automação");
            model.addAttribute("execucoes", new ArrayList<Execucao>());
            return "automacao";
        }
        else {
            return "redirect:/automacoes/true?acessonegado";
        }
    }

    @PostMapping("/automacoes/salvar")
    public String salvarAutomacao(Automacao automacao, Model model, RedirectAttributes ra) {
        try {
            if (!usuarioService.getUsuarioLogado().isAdm()) {
                return "redirect:/automacoes/true?acessonegado";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        boolean novaAutomacao = automacao.getId() == null;

        if (automacao.getHorarioInicio().length() > 0 && automacaoService.isHorarioInvalido(automacao.getHorarioInicio())) {
            automacao.setHorarioInicio(null);
            ra.addFlashAttribute("mensagemErro", "Horário de início inválido");
            if (novaAutomacao) {
                model.addAttribute("automacao", automacao);
                return "redirect:/automacoes/novo";
            }
            else {
                return "redirect:/automacoes/consultar/" + automacao.getId();
            }
        }
        if (automacao.getHorarioFim().length() > 0 && automacaoService.isHorarioInvalido(automacao.getHorarioFim())) {
            automacao.setHorarioFim(null);
            ra.addFlashAttribute("mensagemErro", "Horário fim inválido");
            if (novaAutomacao) {
                model.addAttribute("automacao", automacao);
                return "redirect:/automacoes/novo";
            }
            else {
                return "redirect:/automacoes/consultar/" + automacao.getId();
            }
        }

        try {
            automacaoService.save(automacao);
            ra.addFlashAttribute("mensagemSucesso", "A automação foi salva com sucesso");
        }
        catch (Exception e) {
            ra.addFlashAttribute("mensagemErro", String.format("O nome %s já está sendo usado", automacao.getNome()));
            if (novaAutomacao) {
                model.addAttribute("automacao", automacao);
                return "redirect:/automacoes/novo";
            }
            else {
                return "redirect:/automacoes/consultar/" + automacao.getId();
            }
        }
        return "redirect:/automacoes/true";
    }
}
