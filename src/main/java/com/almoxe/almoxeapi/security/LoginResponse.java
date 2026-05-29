package com.almoxe.almoxeapi.security;

import com.almoxe.almoxeapi.usuario.Papel;

public record LoginResponse(String token, String email, Papel papel) {}
