package api.service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Usuario;
import api.repository.UsuarioRepository;

@Component
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RegraNegocioException("Nenhum usuário autenticado encontrado no contexto.");
        }

        Long id = Long.parseLong(authentication.getName());

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado na base de dados."));
    }

}
