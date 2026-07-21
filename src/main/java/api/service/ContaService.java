package api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import api.dto.AtualizarContaRequestDTO;
import api.dto.ContaRequestDTO;
import api.dto.ContaResponseDTO;
import api.exception.AcessoNegadoException;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Usuario;
import api.repository.ContaRepository;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private AuthService authService;

    public ContaResponseDTO criar(ContaRequestDTO dto) {

        Usuario usuario = authService.buscarUsuarioLogado();

        Conta conta = new Conta();
        conta.setUsuario(usuario);
        conta.setNumeroConta(dto.getNumeroConta());
        conta.setSaldo(dto.getSaldo());
        conta.setTipoConta(dto.getTipoConta());

        Conta salva = contaRepository.save(conta);

        return toDTO(salva);

    }

    public List<ContaResponseDTO> listarTodas() {

        Usuario usuario = authService.buscarUsuarioLogado();

        if(!"ROLE_ADMIN".equals(usuario.getRole())){
            throw new AcessoNegadoException("Acesso negado. Somente admnistradores podem visualizar todas as contas");
        }

        return contaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

    }

    public ContaResponseDTO buscarPorId(Long id) {

        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id: " + id));

        return toDTO(conta);

    }

    public ContaResponseDTO atualizar(AtualizarContaRequestDTO dto) {

        Usuario usuario = authService.buscarUsuarioLogado();

        Conta conta = contaRepository.findById(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id: " + usuario.getId()));

        conta.setTipoConta(dto.getTipoConta());

        Conta atualizada = contaRepository.save(conta);

        return toDTO(atualizada);

    }

    public void deletar(Long id) {

        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id: " + id));

        contaRepository.delete(conta);

    }

    public ContaResponseDTO meusDados() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Conta conta = contaRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        return toDTO(conta);

    }

    private ContaResponseDTO toDTO(Conta conta) {
        ContaResponseDTO dto = new ContaResponseDTO();
        dto.setId(conta.getId());
        dto.setTitular(conta.getUsuario().getNome());
        dto.setEmail(conta.getUsuario().getEmail());
        dto.setNumeroConta(conta.getNumeroConta());
        dto.setSaldo(conta.getSaldo());
        dto.setTipoConta(conta.getTipoConta());
        dto.setAtiva(conta.getAtiva());
        dto.setDataCriacao(conta.getDataCriacao());
        return dto;
    }
}