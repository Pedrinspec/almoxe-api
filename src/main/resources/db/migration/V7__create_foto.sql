CREATE TABLE foto (
    id UUID PRIMARY KEY,
    movimentacao_id UUID NOT NULL REFERENCES movimentacao(id),
    caminho_arquivo VARCHAR(500) NOT NULL,
    data_upload TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_foto_movimentacao ON foto(movimentacao_id);
