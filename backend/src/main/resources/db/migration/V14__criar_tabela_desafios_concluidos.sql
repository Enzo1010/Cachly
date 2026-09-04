CREATE TABLE desafios_concluidos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    desafio_id VARCHAR(255) NOT NULL,
    data_conclusao TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_desafios_concluidos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT uk_desafios_concluidos_usuario_desafio UNIQUE (usuario_id, desafio_id)
);
