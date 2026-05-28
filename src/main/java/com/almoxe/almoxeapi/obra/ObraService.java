package com.almoxe.almoxeapi.obra;

import com.almoxe.almoxeapi.common.RecursoNaoEncontradoException;
import com.almoxe.almoxeapi.common.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ObraService {

    private final ObraRepository repository;

    public ObraService(ObraRepository repository) {
        this.repository = repository;
    }

    public ObraResponse criar(ObraRequest request) {
        Obra obra = new Obra();
        obra.setNome(request.nome());
        obra.setEndereco(request.endereco());
        obra.setAtiva(true);
        return ObraResponse.from(repository.save(obra));
    }

    @Transactional(readOnly = true)
    public List<ObraResponse> listar() {
        return repository.findAll().stream()
                .map(ObraResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ObraResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .map(ObraResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra não encontrada: " + id));
    }

    public ObraResponse atualizar(UUID id, ObraRequest request) {
        Obra obra = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra não encontrada: " + id));
        obra.setNome(request.nome());
        obra.setEndereco(request.endereco());
        return ObraResponse.from(obra);
    }

    public void deletar(UUID id) {
        Obra obra = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra não encontrada: " + id));
        obra.setAtiva(false);
    }

    public Obra buscarAtivaOuFalha(UUID id) {
        Obra obra = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra não encontrada: " + id));
        if (!obra.isAtiva()) {
            throw new RegraNegocioException("Obra inativa não pode receber alocação: " + id);
        }
        return obra;
    }
}
