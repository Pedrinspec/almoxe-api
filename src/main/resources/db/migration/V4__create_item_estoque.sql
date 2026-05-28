CREATE TABLE item_estoque (
    id UUID PRIMARY KEY,
    produto_id UUID NOT NULL REFERENCES produto(id),
    responsavel_id UUID REFERENCES usuario(id),
    status VARCHAR(20) NOT NULL,
    quantidade NUMERIC(12, 3) NOT NULL,
    numero_serie VARCHAR(120),
    lote VARCHAR(120),
    nota_fiscal VARCHAR(120),
    numero_ri VARCHAR(120)
);

CREATE INDEX idx_item_estoque_produto ON item_estoque(produto_id);
