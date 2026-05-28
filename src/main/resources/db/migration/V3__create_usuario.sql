CREATE TABLE usuario (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    senha_hash VARCHAR(100) NOT NULL,
    papel VARCHAR(20) NOT NULL
);

-- Usuários de desenvolvimento (senha de todos: "senha123", hash BCrypt).
-- O cadastro real via endpoint entra junto com Spring Security (marco 9).
INSERT INTO usuario (id, nome, email, senha_hash, papel) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Almoxarife Dev', 'almoxarife@almoxe.dev',
     '$2a$10$fKoKV9zGP1/8FpMzzUjaluae/byHg005SBshr2Ox8UMhyFFlMTzw2', 'ALMOXARIFE'),
    ('22222222-2222-2222-2222-222222222222', 'Líder Dev', 'lider@almoxe.dev',
     '$2a$10$ZJOpoHtmpLguYpH.xm1CAu4xMelBZwUvSaVg26qmDaD313jk/GMgq', 'LIDER');
