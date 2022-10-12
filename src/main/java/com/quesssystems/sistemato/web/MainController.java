package com.quesssystems.sistemato.web;

import com.quesssystems.sistemato.beans.usuario.Usuario;
import com.quesssystems.sistemato.beans.usuario.UsuarioRepository;
import com.quesssystems.sistemato.util.EmailUtil;
import com.quesssystems.sistemato.util.SenhaUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@Controller
public class MainController {
    private final UsuarioRepository repo;
    private final EmailUtil emailUtil;

    public MainController(UsuarioRepository repo, EmailUtil emailUtil) {
        this.repo = repo;
        this.emailUtil = emailUtil;
    }

    @GetMapping("")
    public String iniciar() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }

        return "redirect:/";
    }

    @PostMapping("/recuperarsenha")
    public String recuperarSenha(HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        String email = request.getParameter("email");
        String novaSenha = SenhaUtil.gerarNovaSenha();

        Usuario usuario = repo.findByEmail(email);
        if (usuario == null) {
            return "redirect:/login?emailinvalido";
        }
        usuario.setSenha(SenhaUtil.criptografar(novaSenha));
        repo.save(usuario);

        emailUtil.enviarEmail(email, "Nova senha para acesso ao Sistemato", String.format("<p>Sua senha para acesso ao Sistemato foi atualizada: <b>%s</b></p>", novaSenha));

        return "redirect:/login?senharecuperada";
    }

    @GetMapping("/voltar/{tela}")
    public String voltar(@PathVariable("tela") String tela) {
        switch (tela) {
            case "usuario":
                return "redirect:/usuarios";

            case "usuarios":

            case "automacao":

            default:
                return "redirect:/automacoes/true";
        }
    }
}
