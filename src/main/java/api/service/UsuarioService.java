package api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import api.dto.UsuarioResponseDTO;
import api.exception.ResourceNotFoundException;
import api.model.Usuario;
import api.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnderecoService enderecoService;

    public UsuarioResponseDTO meusDados() {
        Usuario usuario = buscarUsuarioLogado();
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

    public Usuario buscarUsuarioLogado() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Não há um usuário logado"));

    }

}
