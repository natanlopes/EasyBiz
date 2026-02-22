-- V4: Índices de performance para as tabelas principais
-- Contexto: PostgreSQL não cria índice automático em FKs, apenas em PKs e UNIQUE.
-- Todas as FKs abaixo são alvos frequentes de JOIN/WHERE e precisam de índice.

-- negocios: buscas por dono, categoria e status ativo
CREATE INDEX idx_negocios_usuario_id  ON negocios(usuario_id);
CREATE INDEX idx_negocios_categoria   ON negocios(categoria);
CREATE INDEX idx_negocios_ativo       ON negocios(ativo);

-- pedido_servico: buscas por cliente, negocio e status
CREATE INDEX idx_pedido_cliente_id    ON pedido_servico(cliente_id);
CREATE INDEX idx_pedido_negocio_id    ON pedido_servico(negocio_id);
CREATE INDEX idx_pedido_status        ON pedido_servico(status);
-- composto: "pedidos pendentes de um negocio" (query mais comum do dono)
CREATE INDEX idx_pedido_negocio_status ON pedido_servico(negocio_id, status);

-- mensagem: buscas por pedido e ordenacao por data
CREATE INDEX idx_mensagem_pedido_id         ON mensagem(pedido_servico_id);
-- composto: "mensagens de um pedido ordenadas por data" (carregamento do chat)
CREATE INDEX idx_mensagem_pedido_enviado_em ON mensagem(pedido_servico_id, enviado_em);

-- avaliacao: buscas por quem foi avaliado (nota_media do negocio)
CREATE INDEX idx_avaliacao_avaliado_id  ON avaliacao(avaliado_id);
CREATE INDEX idx_avaliacao_pedido_id    ON avaliacao(pedido_id);

-- clientes: buscas por negocio (listagem de clientes do dono)
CREATE INDEX idx_clientes_negocio_id ON clientes(negocio_id);
