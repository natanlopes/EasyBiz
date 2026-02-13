-- V1: Baseline schema - todas as tabelas do EasyBiz

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    nome_completo VARCHAR(255),
    criado_em TIMESTAMP,
    foto_url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS negocios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    categoria VARCHAR(255) NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    ativo BOOLEAN DEFAULT true NOT NULL,
    criado_em TIMESTAMP,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    endereco_completo VARCHAR(255),
    nota_media DOUBLE PRECISION DEFAULT 0.0,
    logo_url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS pedido_servico (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES usuarios(id),
    negocio_id BIGINT NOT NULL REFERENCES negocios(id),
    status VARCHAR(50) NOT NULL,
    descricao TEXT,
    data_desejada TIMESTAMP,
    criado_em TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mensagem (
    id BIGSERIAL PRIMARY KEY,
    pedido_servico_id BIGINT NOT NULL REFERENCES pedido_servico(id),
    remetente_id BIGINT NOT NULL REFERENCES usuarios(id),
    conteudo TEXT NOT NULL,
    enviado_em TIMESTAMP NOT NULL,
    lida BOOLEAN DEFAULT false NOT NULL,
    lida_em TIMESTAMP
);

CREATE TABLE IF NOT EXISTS avaliacao (
    id BIGSERIAL PRIMARY KEY,
    nota INTEGER,
    comentario VARCHAR(255),
    pedido_id BIGINT REFERENCES pedido_servico(id),
    avaliador_id BIGINT REFERENCES usuarios(id),
    avaliado_id BIGINT REFERENCES usuarios(id),
    data_avaliacao TIMESTAMP
);