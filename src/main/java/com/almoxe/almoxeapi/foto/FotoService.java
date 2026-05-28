package com.almoxe.almoxeapi.foto;

import com.almoxe.almoxeapi.common.RecursoNaoEncontradoException;
import com.almoxe.almoxeapi.common.RegraNegocioException;
import com.almoxe.almoxeapi.movimentacao.Movimentacao;
import com.almoxe.almoxeapi.movimentacao.MovimentacaoRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FotoService {

    private final FotoRepository fotoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ArmazenamentoFotos armazenamento;

    public FotoService(FotoRepository fotoRepository,
                       MovimentacaoRepository movimentacaoRepository,
                       ArmazenamentoFotos armazenamento) {
        this.fotoRepository = fotoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.armazenamento = armazenamento;
    }

    public List<FotoResponse> anexar(UUID movimentacaoId, List<MultipartFile> arquivos) {
        Movimentacao movimentacao = movimentacaoRepository.findById(movimentacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimentação não encontrada: " + movimentacaoId));

        if (arquivos == null || arquivos.isEmpty()) {
            throw new RegraNegocioException("Envie ao menos um arquivo de foto.");
        }
        for (MultipartFile arquivo : arquivos) {
            if (arquivo.isEmpty()) {
                throw new RegraNegocioException("Arquivo de foto vazio.");
            }
            if (!armazenamento.tipoSuportado(arquivo.getContentType())) {
                throw new RegraNegocioException(
                        "Tipo de arquivo não suportado: " + arquivo.getContentType()
                                + ". Aceitos: image/jpeg, image/png, image/webp.");
            }
        }

        return arquivos.stream().map(arquivo -> {
            String nomeArquivo = armazenamento.salvar(arquivo);
            Foto foto = new Foto();
            foto.setMovimentacao(movimentacao);
            foto.setCaminhoArquivo(nomeArquivo);
            foto.setDataUpload(Instant.now());
            return FotoResponse.from(fotoRepository.save(foto));
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<FotoResponse> listar(UUID movimentacaoId) {
        if (!movimentacaoRepository.existsById(movimentacaoId)) {
            throw new RecursoNaoEncontradoException("Movimentação não encontrada: " + movimentacaoId);
        }
        return fotoRepository.findByMovimentacaoIdOrderByDataUploadAsc(movimentacaoId).stream()
                .map(FotoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FotoArquivo baixar(UUID fotoId) {
        Foto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Foto não encontrada: " + fotoId));
        Resource conteudo = armazenamento.carregar(foto.getCaminhoArquivo());
        MediaType contentType = MediaTypeFactory.getMediaType(foto.getCaminhoArquivo())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return new FotoArquivo(conteudo, contentType);
    }
}
