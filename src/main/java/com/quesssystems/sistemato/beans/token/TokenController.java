package com.quesssystems.sistemato.beans.token;

import com.quesssystems.sistemato.beans.usuario.Usuario;
import com.quesssystems.sistemato.beans.usuario.UsuarioService;
import com.quesssystems.sistemato.exceptions.TokenNaoEncontradoException;
import com.quesssystems.sistemato.exceptions.UsuarioNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class TokenController {

    @Value("${server.link}")
    private String servidorLink;

    @Value("${server.servlet.context-path}")
    private String servidorContextPath;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;

    public TokenController(TokenService tokenService, UsuarioService usuarioService) {
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/tokens/{ativo}")
    public String listarTokens(@PathVariable("ativo") boolean ativo, Model model) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
            model.addAttribute("adm", usuarioLogado.isAdm());
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        if (usuarioLogado.isAdm()) {
            model.addAttribute("tokens", tokenService.listAll(ativo));
            model.addAttribute("ativos", ativo);
            model.addAttribute("link", servidorLink);
            model.addAttribute("contextPath", servidorContextPath);
            model.addAttribute("pagina", "tokens");

            return "tokens";
        }
        else {
            model.addAttribute("pagina", "automacoes");
            return "redirect:/automacoes/true?acessonegado";
        }
    }

    @GetMapping("/tokens/consultar/{id}")
    public String consultarToken(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
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
            Token token = tokenService.get(id);
            model.addAttribute("tituloPagina", String.format("Token %s", token.getNome()));
            model.addAttribute("token", token);
            model.addAttribute("pagina", "token");
            return "token";
        }
        catch (TokenNaoEncontradoException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            model.addAttribute("pagina", "tokens");
            return "redirect:/tokens/true";
        }
    }

    @GetMapping("/tokens/ativotoggle/{id}")
    public String ativoToggleToken(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
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
            boolean ativada = tokenService.ativoToggle(id);
            String mensagem = "O token %d foi ativado";
            if (!ativada) {
                mensagem = "O token %d foi inativado";
            }
            ra.addFlashAttribute("mensagemSucesso", String.format(mensagem, id));
        }
        catch (TokenNaoEncontradoException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }
        model.addAttribute("pagina", "tokens");
        return "redirect:/tokens/true";
    }

    @GetMapping("/tokens/excluir/{id}")
    public String excluirToken(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
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
            tokenService.delete(id);
            ra.addFlashAttribute("mensagemSucesso", String.format("O token %d foi deletado", id));
        }
        catch (TokenNaoEncontradoException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }
        model.addAttribute("pagina", "tokens");
        return "redirect:/tokens/true";
    }

    @GetMapping("/tokens/novo")
    public String adicionarToken(Model model) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
            model.addAttribute("adm", usuarioLogado.isAdm());
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        if (usuarioLogado.isAdm()) {
            model.addAttribute("token", new Token());
            model.addAttribute("tituloPagina", "Adicionar token");
            model.addAttribute("pagina", "token");
            return "token";
        }
        else {
            model.addAttribute("pagina", "automacoes");
            return "redirect:/automacoes/true?acessonegado";
        }
    }

    @PostMapping("/tokens/salvar")
    public String salvarToken(Token token, Model model, RedirectAttributes ra) {
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

        String mensagemErro = String.format("O nome %s já está sendo usado", token.getNome());
        boolean novoToken = token.getId() == null;

        try {
            if (novoToken) {
                String codigo = String.valueOf(UUID.randomUUID());
                for (int i = 0; i < 3; i++) {
                    if (tokenService.isCodigoValido(codigo)) {
                        token.setCodigo(codigo);
                        break;
                    }
                }

                if (!tokenService.isCodigoValido(codigo)) {
                    mensagemErro = "Falha ao gerar o código";
                }
            }
            tokenService.save(token);
            ra.addFlashAttribute("mensagemSucesso", "O token foi salvo com sucesso");
        }
        catch (Exception e) {
            ra.addFlashAttribute("mensagemErro", mensagemErro);
            if (novoToken) {
                model.addAttribute("token", token);
                model.addAttribute("pagina", "token");
                return "redirect:/tokens/novo";
            }
            else {
                model.addAttribute("pagina", "token");
                return "redirect:/tokens/consultar/" + token.getId();
            }
        }
        model.addAttribute("pagina", "tokens");
        return "redirect:/tokens/true";
    }
}
