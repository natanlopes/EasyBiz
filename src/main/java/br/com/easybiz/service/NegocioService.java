package br.com.easybiz.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.easybiz.exception.ForbiddenException;
import br.com.easybiz.exception.ResourceNotFoundException;
import br.com.easybiz.model.Negocio;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.NegocioRepository;
import br.com.easybiz.repository.UsuarioRepository;

@Service
public class NegocioService {

    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;

    public NegocioService(NegocioRepository negocioRepository,
                          UsuarioRepository usuarioRepository) {
        this.negocioRepository = negocioRepository;
        this.usuarioRepository = usuarioRepository;
    }
    @Transactional
    public Negocio criarNegocio(Long usuarioId, String nome, String categoria,
                                Double latitude, Double longitude, String enderecoCompleto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        Negocio negocio = new Negocio();
        negocio.setNome(nome);
        negocio.setCategoria(categoria.toUpperCase());
        negocio.setUsuario(usuario);
        negocio.setAtivo(true);
        negocio.setLatitude(latitude);
        negocio.setLongitude(longitude);
        negocio.setEnderecoCompleto(enderecoCompleto);


        return negocioRepository.save(negocio);
    }

    private static final List<String> DICIONARIO_CATEGORIAS = List.of(
            "PEDREIRO", "ELETRICISTA", "ENCANADOR", "MECANICO",
            "PINTOR", "MARCENEIRO", "BARBEIRO", "MANICURE",
            "FAXINA", "JARDINAGEM", "FRETE", "TECNICO",
            "MOTORISTA", "ENTREGADOR"
    );

    public List<Negocio> buscarNegocios(Double lat, Double lon, String termoBusca) {
        Double raioKm = 30.0;
        String categoriaCorrigida = corrigirCategoria(termoBusca);

        return negocioRepository.buscarInteligente(lat, lon, raioKm, categoriaCorrigida);
    }

    private String corrigirCategoria(String termo) {
        if (termo == null || termo.isBlank()) {
            return null;
        }

        String normalizado = termo.toUpperCase().trim();

        for (String categoria : DICIONARIO_CATEGORIAS) {
            if (categoria.contains(normalizado) || normalizado.contains(categoria)) {
                return categoria;
            }
        }

        return termo;
    }
    @Transactional
    public void atualizarLocalizacao(Long negocioId, Long usuarioLogadoId,
                                      Double latitude, Double longitude, String enderecoCompleto) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio nao encontrado"));

        if (!negocio.getUsuario().getId().equals(usuarioLogadoId)) {
            throw new ForbiddenException("Acesso negado: Voce nao e o dono deste negocio.");
        }

        negocio.setLatitude(latitude);
        negocio.setLongitude(longitude);
        negocio.setEnderecoCompleto(enderecoCompleto);
        negocioRepository.save(negocio);
    }

    @Transactional
    public void atualizarLogo(Long negocioId, Long usuarioLogadoId, String novaUrl) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio nao encontrado"));

        if (!negocio.getUsuario().getId().equals(usuarioLogadoId)) {
            throw new ForbiddenException("Acesso negado: Voce nao e o dono deste negocio.");
        }

        negocio.setLogoUrl(novaUrl);
        negocioRepository.save(negocio);
    }
}
