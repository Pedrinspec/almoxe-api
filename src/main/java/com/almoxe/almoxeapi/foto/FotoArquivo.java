package com.almoxe.almoxeapi.foto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record FotoArquivo(Resource conteudo, MediaType contentType) {
}
