-- Remove restrições NOT NULL de colunas que foram removidas do formulário
ALTER TABLE ficha_tecnica ALTER COLUMN descricao VARCHAR(255) NULL;

-- Migra enum tipo: remove constraint antiga e atualiza valores ACESSORIO para ACESSORIO_UNIDADE
ALTER TABLE ficha_tecnica ALTER COLUMN tipo VARCHAR(255) NOT NULL;
UPDATE ficha_tecnica SET tipo = 'ACESSORIO_UNIDADE' WHERE tipo = 'ACESSORIO';

-- Remove constraint de enum nas colunas de amostra para aceitar CANCELADO
ALTER TABLE ficha_tecnica ALTER COLUMN status_amostra_cor VARCHAR(255);
ALTER TABLE ficha_tecnica ALTER COLUMN status_amostra_producao VARCHAR(255);
