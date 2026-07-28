package api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import api.dto.ContaResponseDTO;
import api.enums.TipoConta;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Usuario;
import api.repository.ContaRepository;
import api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Transactional
    public ContaResponseDTO criar(Usuario usuario) {

        boolean possui = contaRepository.existsByUsuarioEmail(usuario.getEmail());

        if (possui) {
            throw new RegraNegocioException("O usuário já possui uma conta");
        }

        Conta conta = new Conta();
        conta.setTipoConta(TipoConta.PAGAMENTO);
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

    public ContaResponseDTO desativar(Long id) {

        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id " + id));

        conta.setAtiva(false);

        Conta salva = contaRepository.save(conta);

        return toDTO(salva);

    }

    public ContaResponseDTO ativar(Long id) {

        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id " + id));

        conta.setAtiva(true);

        Conta salva = contaRepository.save(conta);

        return toDTO(salva);

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