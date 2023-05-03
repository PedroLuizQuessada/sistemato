package com.quesssystems.sistemato.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
public class FileUtil {
    @Value("${sistemato.arquivos.pendencias-path}")
    private String arquivosPendenciasPath;
    private final DataFormatter formatter = new DataFormatter();
    public File multipartToFile(MultipartFile multipartFile, String path) throws IOException {
        String nomeArquivo = multipartFile.getOriginalFilename();
        if (nomeArquivo == null) {
            throw new IOException();
        }

        String extensaoArquivo = getExtensaoArquivo(nomeArquivo);
        criarDiretorio(path);
        while (new File(path + nomeArquivo).exists()) {
            nomeArquivo = nomeArquivo.replace(extensaoArquivo, "_1" + extensaoArquivo);
        }
        File file = new File(path + nomeArquivo);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();

        return file;
    }

    public List<List<List<String>>> lerPlanilha(File arquivo) throws IOException {
        List<List<List<String>>> planilhas = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream(arquivo);
        XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(arquivo.toPath()));

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            List<List<String>> planilha = new ArrayList<>();
            XSSFSheet sheet = workbook.getSheetAt(i);

            for (Row row : sheet) {
                List<String> linha = new ArrayList<>();
                Iterator<Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String valor = formatter.formatCellValue(cell);

                    if (valor.length() > 0) {
                        linha.add(valor);
                    }
                }

                if (!linha.isEmpty()) {
                    planilha.add(linha);
                }
            }
            planilhas.add(planilha);
        }
        fileInputStream.close();
        return planilhas;
    }

    public void apagarArquivo(String path) throws IOException {
        Files.delete(Paths.get(path));
    }

    public List<String> listarArquivos(String path) throws IOException {
        List<String> arquivos = new ArrayList<>();
        File diretorio = new File(path);
        criarDiretorio(path);
        if (diretorio.exists() && diretorio.listFiles() != null) {
            for (File arquivo : Objects.requireNonNull(diretorio.listFiles())) {
                arquivos.add(arquivo.getName());
            }
        }

        return arquivos;
    }

    public ResponseEntity<Resource> download(String path, String arquivo) throws IOException {
        File file = new File(path);
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + arquivo);
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        Path pathCompleto = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(pathCompleto));

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    public boolean isNomeArquivoValido(String arquivo) {
        if (arquivo.contains("ç") || arquivo.contains("Ç") || arquivo.contains("ã") || arquivo.contains("Ã") ||
                arquivo.contains("á") || arquivo.contains("Á") || arquivo.contains("à") || arquivo.contains("À") ||
                arquivo.contains("â") || arquivo.contains("Â") || arquivo.contains("é") || arquivo.contains("É") ||
                arquivo.contains("è") || arquivo.contains("È") || arquivo.contains("ê") || arquivo.contains("Ê") ||
                arquivo.contains("í") || arquivo.contains("Í") || arquivo.contains("ì") || arquivo.contains("Ì") ||
                arquivo.contains("î") || arquivo.contains("Î") || arquivo.contains("õ") || arquivo.contains("Õ") ||
                arquivo.contains("ó") || arquivo.contains("Ó") || arquivo.contains("ò") || arquivo.contains("Ò") ||
                arquivo.contains("ô") || arquivo.contains("Ô") || arquivo.contains("ú") || arquivo.contains("Ú") ||
                arquivo.contains("ù") || arquivo.contains("Ù") || arquivo.contains("û") || arquivo.contains("Û")) {
            return false;
        }

        return true;
    }

    private String getExtensaoArquivo(String nomeArquivo) {
        int index = nomeArquivo.lastIndexOf(".");
        return nomeArquivo.substring(index);
    }

    private void criarDiretorio(String path) throws IOException {
        if (!new File(path).exists()) {
            if (!new File(path).mkdirs()) {
                throw new IOException();
            }
        }
    }

    public String getArquivosPendenciasPath() {
        return arquivosPendenciasPath;
    }
}
