UPDATE questoes
SET ativa = false
WHERE id NOT IN (SELECT questao_id FROM alternativas);
