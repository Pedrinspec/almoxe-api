package com.almoxe.almoxeapi.usuario;

import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, Papel papel) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel());
    }
}
