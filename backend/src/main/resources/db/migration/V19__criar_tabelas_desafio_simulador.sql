CREATE TABLE desafios_simulador (
    id VARCHAR(255) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    dificuldade VARCHAR(20) NOT NULL,
    xp_recompensa INTEGER NOT NULL,
    configuracao_json JSONB NOT NULL,
    pergunta TEXT NOT NULL,
    opcao_correta_id VARCHAR(5) NOT NULL,
    dica TEXT NOT NULL,
    explicacao TEXT NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    criado_por BIGINT,
    atualizado_por BIGINT
);

CREATE TABLE desafios_simulador_opcoes (
    desafio_id VARCHAR(255) NOT NULL REFERENCES desafios_simulador(id) ON DELETE CASCADE,
    letra VARCHAR(5) NOT NULL,
    texto TEXT NOT NULL,
    PRIMARY KEY (desafio_id, letra)
);
