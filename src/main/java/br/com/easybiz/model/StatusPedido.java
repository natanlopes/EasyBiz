package br.com.easybiz.model;

public enum StatusPedido {
    ABERTO,        // Criado pelo cliente
    EM_NEGOCIACAO, // Conversando no chat
    ACEITO,        // Dono aceitou
    RECUSADO,      // Dono recusou
    CONCLUIDO,     // Serviço finalizado
    CANCELADO
}

