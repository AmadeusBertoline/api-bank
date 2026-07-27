package api.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import api.exception.ResourceNotFoundException;
import api.model.Usuario;
import api.repository.UsuarioRepository;

public class UsuarioLogado {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario buscarUsuarioLogado() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email do usuário incorreto"));

    }
}
