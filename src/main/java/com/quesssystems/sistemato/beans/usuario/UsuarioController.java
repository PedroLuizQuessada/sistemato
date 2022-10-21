package com.quesssystems.sistemato.beans.usuario;

import com.quesssystems.sistemato.exceptions.UsuarioNaoEncontradoException;
import com.quesssystems.sistemato.util.SenhaUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        if (usuarioLogado.isAdm()) {
            List<Usuario> listUsuarios = usuarioService.listAll();
            model.addAttribute("usuarios", listUsuarios);
            return "usuarios";
        }
        else {
            return "redirect:/automacoes/true?acessonegado";
        }
    }

    @GetMapping("/usuarios/consultar/{id}")
    public String consultarUsuario(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
        try {
            if (!usuarioService.getUsuarioLogado().isAdm()) {
                return "redirect:/automacoes/true?acessonegado";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            Usuario usuario = usuarioService.get(id);
            model.addAttribute("usuario", usuario);
            model.addAttribute("tituloPagina", String.format("Usuário %d", usuario.getId()));
            return "usuario";
        }
        catch (UsuarioNaoEncontradoException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @GetMapping("/usuarios/admtoggle/{id}")
    public String admToggleUsuario(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            if (!usuarioService.getUsuarioLogado().isAdm() || id == 1) {
                return "redirect:/automacoes/true?acessonegado";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            boolean adm = usuarioService.admToggle(id);
            String mensagem = "O usuário %d se tornou ADM";
            if (!adm) {
                mensagem = "O usuário %d não é mais ADM";
            }
            ra.addFlashAttribute("mensagemSucesso", String.format(mensagem, id));
        }
        catch (UsuarioNaoEncontradoException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/excluir/{id}")
    public String excluirUsuario(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            if (!usuarioService.getUsuarioLogado().isAdm() || id == 1) {
                return "redirect:/automacoes/true?acessonegado";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            usuarioService.delete(id);
            ra.addFlashAttribute("mensagemSucesso", String.format("O usuário %d foi deletado", id));

            if (usuarioService.getUsuarioLogado() == null) {
                return "login";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/novo")
    public String adicionarUsuario(Model model) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        if (usuarioLogado.isAdm()) {
            model.addAttribute("usuario", new Usuario());
            model.addAttribute("tituloPagina", "Adicionar usuário");
            return "usuario";
        }
        else {
            return "redirect:/automacoes/true?acessonegado";
        }
    }

    @PostMapping("/usuarios/salvar")
    public String salvarUsuario(Usuario usuario, Model model, RedirectAttributes ra) {
        try {
            if (!usuarioService.getUsuarioLogado().isAdm()) {
                return "redirect:/automacoes/true?acessonegado";
            }
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        boolean novoUsuario = usuario.getId() == null;

        if (usuario.getSenha().length() == 0) {
            try {
                usuario.setSenha(usuarioService.get(usuario.getId()).getSenha());
            }
            catch (UsuarioNaoEncontradoException exception) {
                ra.addFlashAttribute("mensagemErro", exception.getMessage());
                return "redirect:/usuarios";
            }
        }
        else {
            usuario.setSenha(SenhaUtil.criptografar(usuario.getSenha()));
        }

        try {
            if (usuario.getId() != null && usuario.getId() == 1) {
                usuario.setAdm(true);
            }
            usuarioService.save(usuario);
            ra.addFlashAttribute("mensagemSucesso", "O usuário foi salvo com sucesso");
        }
        catch (Exception e) {
            ra.addFlashAttribute("mensagemErro", String.format("O e-mail %s já está sendo usado", usuario.getEmail()));
            if (novoUsuario) {
                model.addAttribute("automacao", usuario);
                return "redirect:/usuarios/novo";
            }
            else {
                return "redirect:/usuarios/consultar/" + usuario.getId();
            }
        }
        return "redirect:/usuarios";
    }
}
