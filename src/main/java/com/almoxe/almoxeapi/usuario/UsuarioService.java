package com.almoxe.almoxeapi.usuario;

import com.almoxe.almoxeapi.common.RecursoNaoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse criar(CriarUsuarioRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setPapel(request.papel());
        UsuarioResponse criado = UsuarioResponse.from(repository.save(usuario));
        log.info("Usuário criado: id={} email={} papel={}", criado.id(), criado.email(), criado.papel());
        return criado;
    }

    public List<UsuarioResponse> listar() {
        return repository.findAll().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    public UsuarioResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }
}
