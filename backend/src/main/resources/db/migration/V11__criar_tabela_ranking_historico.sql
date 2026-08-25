CREATE TABLE ranking_semanal_historico (
    id SERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    xp_final INTEGER NOT NULL,
    posicao INTEGER NOT NULL,
    data_semana DATE NOT NULL,
    CONSTRAINT fk_ranking_historico_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
