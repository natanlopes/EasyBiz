-- Adiciona controle de versao para Optimistic Locking
ALTER TABLE pedido_servico ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
