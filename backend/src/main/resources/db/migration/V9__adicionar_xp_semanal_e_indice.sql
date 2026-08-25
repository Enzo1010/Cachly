ALTER TABLE usuarios
ADD COLUMN xp_semanal INTEGER NOT NULL DEFAULT 0;

CREATE INDEX idx_usuarios_perfil_xp_semanal
ON usuarios (perfil, xp_semanal DESC);
