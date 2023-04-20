package com.quesssystems.sistemato.beans.automacao;

import com.quesssystems.sistemato.beans.log.Log;
import com.quesssystems.sistemato.beans.log.LogService;
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
import java.util.List;

@Controller
public class AutomacaoController {

    @Value("${server.link}")
    private String servidorLink;

    @Value("${server.servlet.context-path}")
    private String servidorContextPath;

    private final AutomacaoService automacaoService;
    private final UsuarioService usuarioService;

    private final LogService logService;

    public AutomacaoController(AutomacaoService automacaoService, UsuarioService usuarioService, LogService logService) {
        this.automacaoService = automacaoService;
        this.usuarioService = usuarioService;
        this.logService = logService;
    }

    @GetMapping("/automacoes/{ativo}")
    public String listarAutomacoes(@PathVariable("ativo") boolean ativo, Model model) {
        try {
            model.addAttribute("adm", usuarioService.getUsuarioLogado().isAdm());
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        model.addAttribute("automacoes", automacaoService.listAll(ativo));
        model.addAttribute("ativos", ativo);
        model.addAttribute("link", servidorLink);
        model.addAttribute("contextPath", servidorContextPath);
        model.addAttribute("pagina", "automacoes");

        return "automacoes";
    }

    @GetMapping("/automacoes/consultar/{id}")
    public String consultarAutomacao(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("adm", usuarioService.getUsuarioLogado().isAdm());
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            Automacao automacao = automacaoService.get(id);

            List<Log> logs = logService.listAll(automacao);
            model.addAttribute("tituloPagina", String.format("Automação %d", automacao.getId()));
            model.addAttribute("automacao", automacao);
            model.addAttribute("logs", logService.listUltimosRegistros(logs));
            model.addAttribute("numLogs", logs.size());
            model.addAttribute("pagina", "automacao");
            return "automacao";
        }
        catch (AutomacaoNaoEncontradaException e) {
            model.addAttribute("pagina", "automacoes");
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/automacoes/true";
        }
    }

    @GetMapping("/automacoes/ativotoggle/{id}")
    public String ativoToggleAutomacao(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("adm", usuarioService.getUsuarioLogado().isAdm());
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
        model.addAttribute("pagina", "automacoes");
        return "redirect:/automacoes/true";
    }

    @GetMapping("/automacoes/excluir/{id}")
    public String excluirAutomacao(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
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
            logService.deleteByAutomacao(automacaoService.get(id));
            automacaoService.delete(id);
            ra.addFlashAttribute("mensagemSucesso", String.format("A automação %d foi deletada", id));
        }
        catch (AutomacaoNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }
        model.addAttribute("pagina", "automacoes");
        return "redirect:/automacoes/true";
    }

    @GetMapping("/automacoes/novo")
    public String adicionarAutomacao(Model model) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
            model.addAttribute("adm", usuarioLogado.isAdm());
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        if (usuarioLogado.isAdm()) {
            model.addAttribute("automacao", new Automacao());
            model.addAttribute("tituloPagina", "Adicionar automação");
            model.addAttribute("logs", new ArrayList<Log>());
            model.addAttribute("numLogs", 0);
            model.addAttribute("pagina", "automacao");
            return "automacao";
        }
        else {
            model.addAttribute("pagina", "automacoes");
            return "redirect:/automacoes/true?acessonegado";
        }
    }

    @PostMapping("/automacoes/salvar")
    public String salvarAutomacao(Automacao automacao, Model model, RedirectAttributes ra) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
            model.addAttribute("adm", usuarioLogado.isAdm());
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
                model.addAttribute("pagina", "automacao");
                return "redirect:/automacoes/novo";
            }
            else {
                model.addAttribute("pagina", "automacao");
                return "redirect:/automacoes/consultar/" + automacao.getId();
            }
        }
        if (automacao.getHorarioFim().length() > 0 && automacaoService.isHorarioInvalido(automacao.getHorarioFim())) {
            automacao.setHorarioFim(null);
            ra.addFlashAttribute("mensagemErro", "Horário fim inválido");
            if (novaAutomacao) {
                model.addAttribute("automacao", automacao);
                model.addAttribute("pagina", "automacao");
                return "redirect:/automacoes/novo";
            }
            else {
                model.addAttribute("pagina", "automacao");
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
                model.addAttribute("pagina", "automacoes");
                return "redirect:/automacoes/novo";
            }
            else {
                model.addAttribute("pagina", "automacao");
                return "redirect:/automacoes/consultar/" + automacao.getId();
            }
        }
        model.addAttribute("pagina", "automacoes");
        return "redirect:/automacoes/true";
    }

    @GetMapping("/automacoes/apagarlogs/{id}")
    public String apagarLogsAutomacao(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
            model.addAttribute("adm", usuarioLogado.isAdm());
            if (!usuarioLogado.isAdm()) {
                return "redirect:/automacoes/true?acessonegado";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            logService.deleteByAutomacao(automacaoService.get(id));
            ra.addFlashAttribute("mensagemSucesso", "Os logs da automação foram apagados");
            model.addAttribute("pagina", "automacao");
            return String.format("redirect:/automacoes/consultar/%d", id);
        }
        catch (AutomacaoNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            model.addAttribute("pagina", "automacoes");
            return "redirect:/automacoes/true";
        }
    }
}
