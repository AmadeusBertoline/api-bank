package api.service;

import org.springframework.stereotype.Service;
import api.dto.UsuarioAtualizaEmailRequestDTO;
import api.dto.UsuarioResponseDTO;
import api.enums.StatusConta;
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

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        if (usuario.getConta().getStatus().equals(StatusConta.BLOQUEADA)) {

            throw new RegraNegocioException(
                    "Sua conta está bloqueada, você não pode realizar transações nem alterações");

        }

        if (!usuario.getEmail().equalsIgnoreCase(dto.email())) {
            if (usuarioRepository.existsByEmail(dto.email())) {
                throw new RegraNegocioException("Esse e-mail já pertence a outro usuário");
            }

            usuario.setEmail(dto.email());

        }

        Usuario salvo = usuarioRepository.save(usuario);
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

        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getCpf(),
                usuario.getDataNascimento(), usuario.getRole(), usuario.getDataCriacao(),
                enderecoService.toDTO(usuario.getEndereco()));
    }

}
