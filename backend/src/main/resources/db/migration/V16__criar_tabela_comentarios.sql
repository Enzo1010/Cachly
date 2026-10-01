CREATE TABLE comentarios (
    id BIGSERIAL PRIMARY KEY,
    questao_id BIGINT NOT NULL REFERENCES questoes(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    conteudo TEXT NOT NULL,
    upvotes INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_comentarios_questao ON comentarios(questao_id);
