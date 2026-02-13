package br.com.easybiz.dto;

import br.com.easybiz.model.Negocio;

public record NegocioResponseDTO(
        Long id,
        String nome,
        String categoria,
        Long usuarioId,
        String nomeUsuario,
        Boolean ativo,
        Double latitude,
        Double longitude,
        String enderecoCompleto,
        Double notaMedia,
        String logoUrl
) {
    public static NegocioResponseDTO fromEntity(Negocio negocio) {
        return new NegocioResponseDTO(
                negocio.getId(),
                negocio.getNome(),
                negocio.getCategoria(),
                negocio.getUsuario().getId(),
                negocio.getUsuario().getNomeCompleto(),
                negocio.getAtivo(),
                negocio.getLatitude(),
                negocio.getLongitude(),
                negocio.getEnderecoCompleto(),
                negocio.getNotaMedia(),
                negocio.getLogoUrl()
        );
    }
}
