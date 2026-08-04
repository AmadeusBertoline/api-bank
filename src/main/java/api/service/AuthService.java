package api.service;

import api.dto.ContaRequestDTO;
import api.dto.LoginRequestDTO;
import api.dto.LoginResponseDTO;
import api.dto.UsuarioRequestDTO;
import api.enums.TipoRole;
import api.exception.RegraNegocioException;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.UsuarioRepository;
import api.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ContaService contaService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ContaService contaService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.contaService = contaService;
    }

    @Transactional
    public String registrarUsuario(UsuarioRequestDTO dto, TipoRole role) {

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RegraNegocioException("Email já cadastrado: " + dto.getEmail());
        }

        if (usuarioRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new RegraNegocioException("CPF já cadastrado: " + dto.getCpf());
        }

        if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
            throw new RegraNegocioException("As senhas devem ser iguais");
        }

        Endereco endereco = new Endereco();
        endereco.setLogradouro(dto.getEndereco().getLogradouro());
        endereco.setNumero(dto.getEndereco().getNumero());
        endereco.setComplemento(dto.getEndereco().getComplemento());
        endereco.setBairro(dto.getEndereco().getBairro());
        endereco.setCidade(dto.getEndereco().getCidade());
        endereco.setUf(dto.getEndereco().getUf());
        endereco.setCep(dto.getEndereco().getCep());

        Usuario usuario = new Usuario();

        usuario.setRole(role);

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCpf(dto.getCpf());
        usuario.setDataNascimento(dto.getDataNascimento());
        usuario.setEndereco(endereco);

        endereco.setUsuario(usuario);

        usuarioRepository.save(usuario);

        if (role.equals(TipoRole.ROLE_USUARIO)) {
            ContaRequestDTO contaRequestDTO = new ContaRequestDTO();
            contaRequestDTO.setUsuario(usuario);
            contaService.criar(contaRequestDTO);
        }

        return "Usuário registrado com sucesso";

    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RegraNegocioException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new RegraNegocioException("Email ou senha inválidos");
        }

        String token = jwtService.gerarToken(usuario.getId(), usuario.getEmail(), usuario.getRole());

        return new LoginResponseDTO(token, "Bearer", usuario.getNome(), usuario.getRole());
    }

}