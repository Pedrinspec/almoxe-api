CREATE TABLE obra (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    endereco VARCHAR(250),
    ativa BOOLEAN NOT NULL
);

ALTER TABLE movimentacao
    ADD CONSTRAINT fk_movimentacao_obra FOREIGN KEY (obra_id) REFERENCES obra(id);
