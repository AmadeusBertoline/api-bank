package api.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ContaService contaService;

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
            contaService.criar(usuario);
        }

        return "Usuário registrado com sucesso";
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RegraNegocioException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new RegraNegocioException("Email ou senha inválidos");
        }

        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getRole());

        return new LoginResponseDTO(token, "Bearer", usuario.getNome(), usuario.getRole());
    }

}