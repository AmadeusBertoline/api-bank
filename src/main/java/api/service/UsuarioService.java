package api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import api.dto.usuario.UsuarioAtualizaEmailRequestDTO;
import api.dto.usuario.UsuarioAtualizaSenhaRequestDTO;
import api.dto.usuario.UsuarioResponseDTO;
import api.enums.StatusConta;
import api.exception.RegraNegocioException;
import api.model.Usuario;
import api.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final EnderecoService enderecoService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioAutenticadoService usuarioAutenticadoService,
            EnderecoService enderecoService,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.enderecoService = enderecoService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO atualizarSenha(UsuarioAtualizaSenhaRequestDTO dto) {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new RegraNegocioException("A senha atual está incorreta");
        }

        if(!dto.senhaNova().equals(dto.senhaRepetida())){
            throw new RegraNegocioException("A nova senha deve ser igual a confirmação de senha");
        }

        usuario.setSenha(passwordEncoder.encode(dto.senhaNova()));

        Usuario salvo = usuarioRepository.save(usuario);

        return toDTO(salvo);

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
