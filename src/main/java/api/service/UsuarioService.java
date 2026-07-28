package api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import api.dto.UsuarioResponseDTO;
import api.model.Usuario;

@Service
public class UsuarioService {

    @Autowired
    private AuthService authService;

    @Autowired
    private EnderecoService enderecoService;

    public UsuarioResponseDTO meusDados() {
        Usuario usuario = authService.buscarUsuarioLogado();
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
