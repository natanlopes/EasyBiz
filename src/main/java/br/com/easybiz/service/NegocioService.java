package br.com.easybiz.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.easybiz.model.Negocio;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.NegocioRepository;
import br.com.easybiz.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class NegocioService {

    private NegocioRepository negocioRepository;
    private UsuarioRepository usuarioRepository;

    public NegocioService(NegocioRepository negocioRepository,
                          UsuarioRepository usuarioRepository) {
        this.negocioRepository = negocioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Negocio criarNegocio(Long usuarioId, String nome, String categoria) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Negocio negocio = new Negocio();
        negocio.setNome(nome);
        negocio.setCategoria(categoria.toUpperCase());
        negocio.setUsuario(usuario);
        negocio.setAtivo(true);
     // Futuramente: Pegar lat/long do endereço via Google Maps API aqui
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

        return negocioRepository.buscarInteligente(
            lat,
            lon,
            raioKm,
            categoriaCorrigida
        );
    }

    /**
     * Corrige erros simples de digitação
     * Ex: "perdeiro" -> "PEDREIRO"
     */
    private String corrigirCategoria(String termo) {
        if (termo == null || termo.isBlank()) {
			return null;
		}

        String normalizado = termo.toUpperCase().trim();

        // 1. Tenta achar no dicionário (Correção inteligente)
        for (String categoria : DICIONARIO_CATEGORIAS) {
            if (categoria.contains(normalizado) || normalizado.contains(categoria)) {
                return categoria;
            }
        }

        // 2. Se não achou, retorna o termo original! (Para buscar "Adestrador", "Psicólogo", etc)
        return termo;
    }

    public void atualizarLogo(Long negocioId, Long usuarioLogadoId, String novaUrl) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new RuntimeException("Negócio não encontrado"));

        // Verifica se o ID do dono do negócio bate com o ID de quem está logado
        if (!negocio.getUsuario().getId().equals(usuarioLogadoId)) {
            throw new SecurityException("Acesso negado: Você não é o dono deste negócio.");
        }

        negocio.setLogoUrl(novaUrl);
        negocioRepository.save(negocio);
    }

}

