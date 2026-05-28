CREATE TABLE produto (
    id UUID PRIMARY KEY,
    categoria_id UUID REFERENCES categoria(id),
    nome VARCHAR(150) NOT NULL,
    descricao VARCHAR(500),
    tipo_controle VARCHAR(20) NOT NULL,
    estoque_minimo INTEGER NOT NULL
);
