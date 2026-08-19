-- Adiciona colunas de auditoria de autoria nas tabelas administrativas.
-- As colunas são nullable para preservar registros existentes anteriores a esta migration.

ALTER TABLE categorias   ADD COLUMN criado_por     BIGINT REFERENCES usuarios(id);
ALTER TABLE categorias   ADD COLUMN atualizado_por BIGINT REFERENCES usuarios(id);

ALTER TABLE questoes     ADD COLUMN criado_por     BIGINT REFERENCES usuarios(id);
ALTER TABLE questoes     ADD COLUMN atualizado_por BIGINT REFERENCES usuarios(id);

ALTER TABLE alternativas ADD COLUMN criado_por     BIGINT REFERENCES usuarios(id);
ALTER TABLE alternativas ADD COLUMN atualizado_por BIGINT REFERENCES usuarios(id);
