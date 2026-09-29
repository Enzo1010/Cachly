ALTER TABLE usuarios
    ADD COLUMN token_reset VARCHAR(255),
    ADD COLUMN token_reset_expira_em TIMESTAMP WITH TIME ZONE;
