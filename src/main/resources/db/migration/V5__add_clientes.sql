CREATE TABLE IF NOT EXISTS clientes (
                                        id BIGSERIAL PRIMARY KEY,
                                        nome VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefone VARCHAR(255),
    documento VARCHAR(255),
    negocio_id BIGINT NOT NULL REFERENCES negocios(id),
    criado_em TIMESTAMP
    );
