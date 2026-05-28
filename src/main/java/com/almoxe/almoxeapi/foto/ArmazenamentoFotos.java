package com.almoxe.almoxeapi.foto;

import com.almoxe.almoxeapi.common.RegraNegocioException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Component
public class ArmazenamentoFotos {

    private static final Map<String, String> EXTENSAO_POR_TIPO = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path diretorioRaiz;

    public ArmazenamentoFotos(@Value("${almoxe.storage.fotos-dir}") String fotosDir) {
        this.diretorioRaiz = Path.of(fotosDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void inicializar() {
        try {
            Files.createDirectories(diretorioRaiz);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar o diretório de fotos: " + diretorioRaiz, e);
        }
    }

    public boolean tipoSuportado(String contentType) {
        return contentType != null && EXTENSAO_POR_TIPO.containsKey(contentType);
    }

    public String salvar(MultipartFile arquivo) {
        String extensao = EXTENSAO_POR_TIPO.get(arquivo.getContentType());
        String nomeArquivo = UUID.randomUUID() + extensao;
        Path destino = diretorioRaiz.resolve(nomeArquivo).normalize();
        if (!destino.startsWith(diretorioRaiz)) {
            throw new RegraNegocioException("Caminho de arquivo inválido.");
        }
        try {
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar a foto.", e);
        }
        return nomeArquivo;
    }

    public Resource carregar(String nomeArquivo) {
        Path arquivo = diretorioRaiz.resolve(nomeArquivo).normalize();
        if (!arquivo.startsWith(diretorioRaiz)) {
            throw new RegraNegocioException("Caminho de arquivo inválido.");
        }
        try {
            Resource resource = new UrlResource(arquivo.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new UncheckedIOException(new IOException("Arquivo de foto ausente: " + nomeArquivo));
            }
            return resource;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler a foto.", e);
        }
    }
}
