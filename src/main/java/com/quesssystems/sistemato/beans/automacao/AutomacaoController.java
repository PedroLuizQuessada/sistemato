package com.quesssystems.sistemato.beans.automacao;

import com.quesssystems.sistemato.beans.log.Log;
import com.quesssystems.sistemato.beans.log.LogService;
import com.quesssystems.sistemato.beans.pendencia.Pendencia;
import com.quesssystems.sistemato.beans.pendencia.PendenciaService;
import com.quesssystems.sistemato.beans.usuario.Usuario;
import com.quesssystems.sistemato.beans.usuario.UsuarioService;
import com.quesssystems.sistemato.exceptions.AutomacaoNaoEncontradaException;
import com.quesssystems.sistemato.exceptions.PendenciaNaoEncontradaException;
import com.quesssystems.sistemato.exceptions.UsuarioNaoEncontradoException;
import com.quesssystems.sistemato.util.FileUtil;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class AutomacaoController {

    @Value("${server.link}")
    private String servidorLink;

    @Value("${server.servlet.context-path}")
    private String servidorContextPath;

    private final AutomacaoService automacaoService;
    private final UsuarioService usuarioService;
    private final PendenciaService pendenciaService;

    private final LogService logService;

    private final FileUtil fileUtil;

    public AutomacaoController(AutomacaoService automacaoService, UsuarioService usuarioService, PendenciaService pendenciaService, LogService logService, FileUtil fileUtil) {
        this.automacaoService = automacaoService;
        this.usuarioService = usuarioService;
        this.pendenciaService = pendenciaService;
        this.logService = logService;
        this.fileUtil = fileUtil;
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

            List<Log> logs = logService.listAllByAutomacao(automacao);
            List<Pendencia> pendencias = pendenciaService.listAll(automacao);
            model.addAttribute("tituloPagina", String.format("Automação %d", automacao.getId()));
            model.addAttribute("automacao", automacao);
            model.addAttribute("logs", logService.listUltimosRegistros(logs));
            model.addAttribute("numLogs", logs.size());
            model.addAttribute("pendencias", pendenciaService.listUltimosRegistros(pendencias));
            model.addAttribute("numPendencias", pendencias.size());
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
            for (String pendencia : fileUtil.listarArquivos(fileUtil.getArquivosPendenciasPath() + id)) {
                fileUtil.apagarArquivo(fileUtil.getArquivosPendenciasPath() + id + "/" + pendencia);
            }
            fileUtil.apagarArquivo(fileUtil.getArquivosPendenciasPath() + id + "/");

            logService.deleteByAutomacao(automacaoService.get(id));
            pendenciaService.deleteByAutomacao(automacaoService.get(id), false);
            automacaoService.delete(id);
            ra.addFlashAttribute("mensagemSucesso", String.format("A automação %d foi deletada", id));
        }
        catch (IOException e) {
            ra.addFlashAttribute("mensagemErro", "Falha ao apagar arquivos");
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
            model.addAttribute("pendencias", new ArrayList<Pendencia>());
            model.addAttribute("numPendencias", 0);
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
            ra.addFlashAttribute("mensagemSucesso", automacaoService.recuperarTextoAutomacaoSalva(automacaoService.save(automacao)));
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

    @GetMapping("/automacoes/apagarpendencias/{id}/{apagarApenasProcessadas}")
    public String apagarPendenciaAutomacao(@PathVariable("id") Integer id, @PathVariable("apagarApenasProcessadas") boolean apagarApenasProcessadas, Model model, RedirectAttributes ra) {
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
            pendenciaService.deleteByAutomacao(automacaoService.get(id), apagarApenasProcessadas);
            String mensagem = "As pendências da automação foram apagadas";
            if (apagarApenasProcessadas) {
                mensagem = "As pendências processadas da automação foram apagadas";
            }
            ra.addFlashAttribute("mensagemSucesso", mensagem);
            model.addAttribute("pagina", "automacao");
            return String.format("redirect:/automacoes/consultar/%d", id);
        }
        catch (AutomacaoNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            model.addAttribute("pagina", "automacoes");
            return "redirect:/automacoes/true";
        }
    }

    @GetMapping("/automacoes/apagarlogs/{id}")
    public String apagarLogsAutomacao(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
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

    @PostMapping(value = "/automacoes/subirpendencias")
    public String subirPendencias(@RequestParam("id") Integer id, @RequestParam("arquivos") MultipartFile[] arquivos, Model model, RedirectAttributes ra) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
            model.addAttribute("adm", usuarioLogado.isAdm());
        }
        catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        try {
            for (MultipartFile arquivo : arquivos) {
                if (arquivo.getOriginalFilename() == null || (!arquivo.getOriginalFilename().endsWith(".xls") && !arquivo.getOriginalFilename().endsWith(".xlsx"))) {
                    ra.addFlashAttribute("mensagemErro", "Apenas planilhas Excel devem ser anexadas");
                    model.addAttribute("pagina", "automacao");
                    return String.format("redirect:/automacoes/consultar/%d", id);
                }
                if (!fileUtil.isNomeArquivoValido(Objects.requireNonNull(arquivo.getOriginalFilename()))) {
                    ra.addFlashAttribute("mensagemErro", "Caracteres latinos não são permitidos nos nomes dos arquivos");
                    model.addAttribute("pagina", "automacao");
                    return String.format("redirect:/automacoes/consultar/%d", id);
                }
            }

            for (MultipartFile arquivo : arquivos) {
                File file = fileUtil.multipartToFile(arquivo, fileUtil.getArquivosPendenciasPath() + id + "/");
                pendenciaService.save(pendenciaService.converterPendencia(automacaoService.get(id), file.getName(), fileUtil.lerPlanilha(file)));
            }
        }
        catch (SizeLimitExceededException e) {
            ra.addFlashAttribute("mensagemErro", "Arquivo muito grande");
            model.addAttribute("pagina", "automacao");
            return String.format("redirect:/automacoes/consultar/%d", id);
        }
        catch (IOException e) {
            ra.addFlashAttribute("mensagemErro", "Falha ao ler arquivos");
            model.addAttribute("pagina", "automacao");
            return String.format("redirect:/automacoes/consultar/%d", id);
        }
        catch (AutomacaoNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            model.addAttribute("pagina", "automacoes");
            return "redirect:/automacoes/true";
        }

        ra.addFlashAttribute("mensagemSucesso", "Pendências inseridas");
        model.addAttribute("pagina", "automacao");
        return String.format("redirect:/automacoes/consultar/%d", id);
    }

    @PostMapping(value = "/automacoes/apagarpendencias")
    public String apagarPendencias(@RequestParam("id") Integer id, @RequestParam("ids") String ids, Model model, RedirectAttributes ra) {
        Usuario usuarioLogado;
        try {
            usuarioLogado = usuarioService.getUsuarioLogado();
            model.addAttribute("adm", usuarioLogado.isAdm());
        } catch (UsuarioNaoEncontradoException e) {
            return "redirect:/login?sessaoexpirada";
        }

        if (ids.contains(";")) {
            for (String idPendencia : ids.split(";")) {
                try {
                    pendenciaService.get(Integer.valueOf(idPendencia));
                    pendenciaService.delete(Integer.valueOf(idPendencia));
                } catch (PendenciaNaoEncontradaException e) {
                    ra.addFlashAttribute("mensagemErro", e.getMessage());
                    model.addAttribute("pagina", "automacoes");
                    return "redirect:/automacoes/true";
                }
            }
        }

        ra.addFlashAttribute("mensagemSucesso", "Pendências selecionadas apagadas");
        model.addAttribute("pagina", "automacao");
        return String.format("redirect:/automacoes/consultar/%d", id);
    }

    @GetMapping("/automacoes/excluirpendencia/{id}")
    public String excluirPendencia(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
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
            Integer idAutomacao = pendenciaService.get(id).getAutomacao().getId();
            pendenciaService.delete(id);

            ra.addFlashAttribute("mensagemSucesso", "Pendência excluída");
            model.addAttribute("pagina", "automacao");
            return String.format("redirect:/automacoes/consultar/%d", idAutomacao);
        }
        catch (PendenciaNaoEncontradaException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
            model.addAttribute("pagina", "automacao");
            return "redirect:/automacoes/true";
        }
    }
}
