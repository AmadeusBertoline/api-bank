package api.service;

import org.springframework.stereotype.Service;
import api.dto.UsuarioAtualizaEmailRequestDTO;
import api.dto.UsuarioResponseDTO;
import api.exception.RegraNegocioException;
import api.model.Usuario;
import api.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final EnderecoService enderecoService;
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(
            UsuarioAutenticadoService usuarioAutenticadoService,
            EnderecoService enderecoService,
            UsuarioRepository usuarioRepository) {

        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.enderecoService = enderecoService;
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponseDTO atualizarEmail(UsuarioAtualizaEmailRequestDTO dto) {

        Usuario logado = usuarioAutenticadoService.getUsuarioLogado();

        if (!logado.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new RegraNegocioException("Esse e-mail já pertence a outro usuário");
            }

            logado.setEmail(dto.getEmail());

        }

        Usuario salvo = usuarioRepository.save(logado);
        return toDTO(salvo);

    }

    public UsuarioResponseDTO meusDados() {
        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();
        return toDTO(usuario);
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {

        if (usuario == null) {
            return null;
        }

        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setCpf(usuario.getCpf());
        dto.setDataNascimento(usuario.getDataNascimento());
        dto.setRole(usuario.getRole());
        dto.setDataCriacao(usuario.getDataCriacao());

        dto.setEndereco(enderecoService.toDTO(usuario.getEndereco()));

        return dto;

    }

}
