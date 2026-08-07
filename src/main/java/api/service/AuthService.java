package api.service;

import api.dto.ContaRequestDTO;
import api.dto.LoginRequestDTO;
import api.dto.LoginResponseDTO;
import api.dto.UsuarioRequestDTO;
import api.enums.StatusConta;
import api.enums.TipoRole;
import api.exception.RegraNegocioException;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.UsuarioRepository;
import api.security.JwtService;
import jakarta.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
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
    @CacheEvict(value = "usuarios", allEntries = true)
    public String registrarUsuario(UsuarioRequestDTO dto, TipoRole role) {

        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new RegraNegocioException("Email já cadastrado: " + dto.email());
        }

        if (usuarioRepository.findByCpf(dto.cpf()).isPresent()) {
            throw new RegraNegocioException("CPF já cadastrado: " + dto.cpf());
        }

        if (!dto.senha().equals(dto.confirmarSenha())) {
            throw new RegraNegocioException("As senhas devem ser iguais");
        }

        Endereco endereco = new Endereco();
        endereco.setLogradouro(dto.endereco().logradouro());
        endereco.setNumero(dto.endereco().numero());
        endereco.setComplemento(dto.endereco().complemento());
        endereco.setBairro(dto.endereco().bairro());
        endereco.setCidade(dto.endereco().cidade());
        endereco.setUf(dto.endereco().uf());
        endereco.setCep(dto.endereco().cep());

        Usuario usuario = new Usuario();

        usuario.setRole(role);

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setCpf(dto.cpf());
        usuario.setDataNascimento(dto.dataNascimento());
        usuario.setEndereco(endereco);

        endereco.setUsuario(usuario);

        usuarioRepository.save(usuario);

        if (role.equals(TipoRole.ROLE_USUARIO)) {
            ContaRequestDTO contaRequestDTO = new ContaRequestDTO(usuario);
            contaService.criar(contaRequestDTO);
        }

        return "Usuário registrado com sucesso";

    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RegraNegocioException("Email ou senha inválidos"));

        if (usuario.getConta() != null && usuario.getConta().getStatus().equals(StatusConta.ENCERRADA)) {

            throw new RegraNegocioException("Você encerrou sua conta e não pode mais acessar ela");

        }

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new RegraNegocioException("Email ou senha inválidos");
        }

        String token = jwtService.gerarToken(usuario.getId(), usuario.getEmail(), usuario.getRole());

        return new LoginResponseDTO(token, "Bearer", usuario.getId(), usuario.getNome(), usuario.getRole());
    }

}