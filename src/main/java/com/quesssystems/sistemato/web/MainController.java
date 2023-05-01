package com.quesssystems.sistemato.web;

import automacao.AutomacaoApi;
import automacao.PendenciaApi;
import automacao.Requisicao;
import com.quesssystems.sistemato.beans.automacao.Automacao;
import com.quesssystems.sistemato.beans.automacao.AutomacaoRepository;
import com.quesssystems.sistemato.beans.log.Log;
import com.quesssystems.sistemato.beans.log.LogRepository;
import com.quesssystems.sistemato.beans.pendencia.Pendencia;
import com.quesssystems.sistemato.beans.pendencia.PendenciaRepository;
import com.quesssystems.sistemato.beans.pendencia.PendenciaService;
import com.quesssystems.sistemato.beans.token.Token;
import com.quesssystems.sistemato.beans.token.TokenRepository;
import com.quesssystems.sistemato.beans.usuario.Usuario;
import com.quesssystems.sistemato.beans.usuario.UsuarioRepository;
import com.quesssystems.sistemato.util.EmailUtil;
import com.quesssystems.sistemato.util.SenhaUtil;
import enums.StatusEnum;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {
    private final UsuarioRepository usuarioRepository;
    private final AutomacaoRepository automacaoRepository;
    private final LogRepository logRepository;
    private final TokenRepository tokenRepository;
    private final PendenciaRepository pendenciaRepository;
    private final PendenciaService pendenciaService;
    private final EmailUtil emailUtil;

    public MainController(UsuarioRepository usuarioRepository, AutomacaoRepository automacaoRepository, LogRepository logRepository, TokenRepository tokenRepository, PendenciaRepository pendenciaRepository, PendenciaService pendenciaService, EmailUtil emailUtil) {
        this.usuarioRepository = usuarioRepository;
        this.automacaoRepository = automacaoRepository;
        this.logRepository = logRepository;
        this.tokenRepository = tokenRepository;
        this.pendenciaRepository = pendenciaRepository;
        this.pendenciaService = pendenciaService;
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

    @PostMapping("/recuperardados")
    @ResponseBody
    public AutomacaoApi recuperarDados(@RequestBody Requisicao requisicao) {
        AutomacaoApi automacaoApi = identificarToken(requisicao.getToken());
        if (automacaoApi.getStatus().equals(StatusEnum.TOKENINVALIDO)) {
            return automacaoApi;
        }

        return identificarAutomacao(requisicao.getIdAutomacao());
    }

    @PostMapping("/registrarlog")
    @ResponseBody
    public AutomacaoApi registrarLog(@RequestBody Requisicao requisicao) {
        AutomacaoApi automacaoApi = identificarToken(requisicao.getToken());
        if (automacaoApi.getStatus().equals(StatusEnum.TOKENINVALIDO)) {
            return automacaoApi;
        }

        automacaoApi = identificarAutomacao(requisicao.getIdAutomacao());
        if (automacaoApi.getStatus().equals(StatusEnum.OK)) {
            if (requisicao.getMensagem() == null || requisicao.getMensagem().length() == 0) {
                automacaoApi = new AutomacaoApi(StatusEnum.MENSAGEM_INVALIDA);
            }
            else {
                Automacao automacao = automacaoRepository.findById(requisicao.getIdAutomacao()).get();

                Log log = new Log();
                log.setAutomacao(automacao);
                log.setToken(tokenRepository.findByCodigo(requisicao.getToken()));
                log.setHora(new Timestamp(System.currentTimeMillis()));
                log.setMensagem(requisicao.getMensagem());
                logRepository.save(log);
            }
        }
        return automacaoApi;
    }

    @PostMapping("/processarpendencia")
    @ResponseBody
    public AutomacaoApi processarPendencia(@RequestBody Requisicao requisicao) {
        AutomacaoApi automacaoApi = identificarToken(requisicao.getToken());
        if (automacaoApi.getStatus().equals(StatusEnum.TOKENINVALIDO)) {
            return automacaoApi;
        }

        automacaoApi = identificarAutomacao(requisicao.getIdAutomacao());
        if (automacaoApi.getStatus().equals(StatusEnum.OK)) {
            automacaoApi = identificarPendencia(requisicao.getIdPendencia());

            if (automacaoApi.getStatus().equals(StatusEnum.OK)) {
                Pendencia pendencia = pendenciaRepository.findById(requisicao.getIdPendencia()).get();

                pendencia.setProcessado(true);
                pendencia.setDataHoraProcessamento(new Timestamp(System.currentTimeMillis()));
                pendenciaRepository.save(pendencia);
            }
        }
        return automacaoApi;
    }

    private AutomacaoApi identificarToken(String codigo) {
        Token token = tokenRepository.findByCodigo(codigo);
        if (token == null || !token.isAtivo()) {
            return new AutomacaoApi(StatusEnum.TOKENINVALIDO);
        }
        else {
            return new AutomacaoApi(StatusEnum.OK);
        }
    }

    private AutomacaoApi identificarAutomacao(Integer idAutomacao) {
        Optional<Automacao> optionalAutomacao = automacaoRepository.findById(idAutomacao);
        if (optionalAutomacao.isPresent()) {
            Automacao automacao = optionalAutomacao.get();
            return new AutomacaoApi(StatusEnum.OK, automacao.isAtivo(), automacao.isDomingo(),
                    automacao.isSegunda(), automacao.isTerca(), automacao.isQuarta(), automacao.isQuinta(),
                    automacao.isSexta(), automacao.isSabado(), automacao.getHorarioInicio(), automacao.getHorarioFim(),
                    automacao.getEstrutura(), automacao.isHabilitarTexto(), automacao.getTexto(), recuperarPendencias(idAutomacao));
        }
        else {
            return new AutomacaoApi(StatusEnum.NAOENCONTRADO);
        }
    }

    private List<PendenciaApi> recuperarPendencias(Integer idAutomacao) {
        List<PendenciaApi> pendenciaApis = new ArrayList<>();
        List<Pendencia> pendencias = pendenciaRepository.findByAutomacao(idAutomacao);
        pendencias.removeIf(pendencia -> pendencia.isProcessado().equalsIgnoreCase("sim"));
        for (Pendencia pendencia : pendencias) {
            PendenciaApi pendenciaApi = new PendenciaApi();
            pendenciaApi.setId(pendencia.getId());
            pendenciaApi.setJson(pendencia.getJson());
            pendenciaApis.add(pendenciaApi);
        }
        return pendenciaApis;
    }

    private AutomacaoApi identificarPendencia(Integer idPendencia) {
        Optional<Pendencia> optionalPendencia = pendenciaRepository.findById(idPendencia);
        if (optionalPendencia.isPresent()) {
            return new AutomacaoApi(StatusEnum.OK);
        }
        else {
            return new AutomacaoApi(StatusEnum.NAOENCONTRADO);
        }
    }
}
