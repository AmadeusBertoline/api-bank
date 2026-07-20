package api.service;

import api.dto.LoginRequestDTO;
import api.dto.LoginResponseDTO;
import api.dto.RegistroRequestDTO;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Usuario;
import api.repository.UsuarioRepository;
import api.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public String registrar(RegistroRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RegraNegocioException("Email já cadastrado: " + dto.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole(dto.getRole());

        usuarioRepository.save(usuario);
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

    public Usuario buscarUsuarioLogado() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email do usuário incorreto"));

    }

}