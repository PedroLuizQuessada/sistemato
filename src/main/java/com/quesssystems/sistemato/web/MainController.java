package com.quesssystems.sistemato.web;

import automacao.AutomacaoApi;
import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.beans.automacao.AutomacaoRepository;
import com.quesssystems.sistemato.beans.usuario.Usuario;
import com.quesssystems.sistemato.beans.usuario.UsuarioRepository;
import com.quesssystems.sistemato.util.EmailUtil;
import com.quesssystems.sistemato.util.SenhaUtil;
import enums.StatusEnum;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class MainController {
    private final UsuarioRepository usuarioRepository;
    private final AutomacaoRepository automacaoRepository;
    private final EmailUtil emailUtil;

    public MainController(UsuarioRepository usuarioRepository, AutomacaoRepository automacaoRepository, EmailUtil emailUtil) {
        this.usuarioRepository = usuarioRepository;
        this.automacaoRepository = automacaoRepository;
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

        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            return "redirect:/login?emailinvalido";
        }
        usuario.setSenha(SenhaUtil.criptografar(novaSenha));
        usuarioRepository.save(usuario);

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

    @GetMapping("/recuperardados/{idAutomacao}")
    @ResponseBody
    public AutomacaoApi recuperarDados(@PathVariable("idAutomacao") Integer idAutomacao) {
        Optional<Automacao> optionalAutomacao = automacaoRepository.findById(idAutomacao);
        if (optionalAutomacao.isPresent()) {
            Automacao automacao = optionalAutomacao.get();
            return new AutomacaoApi(idAutomacao, StatusEnum.OK, automacao.isAtivo(), automacao.isDomingo(),
                    automacao.isSegunda(), automacao.isTerca(), automacao.isQuarta(), automacao.isQuinta(),
                    automacao.isSexta(), automacao.isSabado(), automacao.getHorarioInicio(), automacao.getHorarioFim());
        }
        else {
            return new AutomacaoApi(StatusEnum.NAOENCONTRADO);
        }
    }
}
