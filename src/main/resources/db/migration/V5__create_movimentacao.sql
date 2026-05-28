CREATE TABLE movimentacao (
    id UUID PRIMARY KEY,
    item_estoque_id UUID NOT NULL REFERENCES item_estoque(id),
    obra_id UUID,
    usuario_id UUID NOT NULL REFERENCES usuario(id),
    tipo VARCHAR(20) NOT NULL,
    quantidade NUMERIC(12, 3) NOT NULL,
    data_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    observacao VARCHAR(500)
);

CREATE INDEX idx_movimentacao_item ON movimentacao(item_estoque_id);
