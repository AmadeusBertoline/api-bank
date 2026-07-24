package api.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import api.dto.AtualizarContaRequestDTO;
import api.dto.ContaRequestDTO;
import api.dto.ContaResponseDTO;
import api.enums.TipoConta;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Usuario;
import api.repository.ContaRepository;
import jakarta.transaction.Transactional;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private AuthService authService;

    @Transactional
    public ContaResponseDTO criar(ContaRequestDTO dto) {

        Usuario usuario = authService.buscarUsuarioLogado();

        boolean possui = contaRepository.existsByUsuarioEmailAndTipoConta(usuario.getEmail(), dto.getTipoConta());

        if (possui) {
            throw new RegraNegocioException("O usuário já possui uma conta do tipo " + dto.getTipoConta());
        }

        Conta conta = new Conta();
        conta.setTipoConta(dto.getTipoConta());
        conta.setUsuario(usuario);
        conta.setNumeroConta("PENDENTE");

        conta = contaRepository.save(conta);

        String numeroConta = String.format("%04d-%d", conta.getId(), calcularDigito(conta.getId()));
        conta.setNumeroConta(numeroConta);

        return toDTO(conta);
    }

    public List<ContaResponseDTO> listarTodas() {

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

    public ContaResponseDTO meusDados(TipoConta tipoConta) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Conta conta = contaRepository.findByUsuarioEmailAndTipoConta(email, tipoConta)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        return toDTO(conta);

    }

    private int calcularDigito(Long id) {
        return (int) (id % 10);
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