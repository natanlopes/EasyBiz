ALTER TABLE avaliacao
    ADD CONSTRAINT uq_avaliacao_pedido UNIQUE (pedido_id);
