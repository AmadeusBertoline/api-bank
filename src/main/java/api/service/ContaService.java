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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class ContaService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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


    public ContaResponseDTO meusDados() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Conta conta = contaRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        return toDTO(conta);

    }

    @Transactional
    public ContaResponseDTO desativar(Long id) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario admin = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin não encontrado"));

        definirVariaveisSessaoSql(admin.getId(), admin.getNome());

        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id " + id));

        conta.setAtiva(false);

        Conta salva = contaRepository.save(conta);

        return toDTO(salva);

    }

    @Transactional
    public ContaResponseDTO ativar(Long id) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario admin = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin não encontrado"));

        definirVariaveisSessaoSql(admin.getId(), admin.getNome());

        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id " + id));

        conta.setAtiva(true);

        Conta salva = contaRepository.save(conta);

        return toDTO(salva);

    }

    private int calcularDigito(Long id) {
        return (int) (id % 10);
    }

    private void definirVariaveisSessaoSql(Long adminId, String adminNome) {
        entityManager.createNativeQuery("SET @admin_id = :id, @admin_nome = :nome")
                .setParameter("id", adminId)
                .setParameter("nome", adminNome)
                .executeUpdate();
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