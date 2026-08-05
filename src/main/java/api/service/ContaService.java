package api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import api.dto.ContaRequestDTO;
import api.dto.ContaResponseDTO;
import api.dto.LimiteRequestDTO;
import api.enums.TipoConta;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Usuario;
import api.repository.ContaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class ContaService {

    @PersistenceContext
    private final EntityManager entityManager;

    private final ContaRepository contaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ContaService(
            ContaRepository contaRepository,
            UsuarioAutenticadoService usuarioAutenticadoService, EntityManager entityManager) {

        this.contaRepository = contaRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.entityManager = entityManager;
    }

    @Transactional
    public ContaResponseDTO criar(ContaRequestDTO usuario) {

        boolean possui = contaRepository.existsByUsuarioEmail(usuario.usuario().getEmail());

        if (possui) {
            throw new RegraNegocioException("O usuário já possui uma conta");
        }

        Conta conta = new Conta();
        conta.setTipoConta(TipoConta.PAGAMENTO);
        conta.setUsuario(usuario.usuario());
        conta.setNumeroConta("PENDENTE");

        conta = contaRepository.save(conta);

        String numeroConta = String.format("%04d-%d", conta.getId(), calcularDigito(conta.getId()));
        conta.setNumeroConta(numeroConta);

        return toDTO(conta);
    }

    public Page<ContaResponseDTO> listarTodas(Pageable pageable) {

        return contaRepository.findAll(pageable).map(this::toDTO);

    }

    public ContaResponseDTO meusDados() {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        return toDTO(conta);

    }

    @Transactional
    public ContaResponseDTO desativarMinhaConta() {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmailWithLock(usuario.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        conta.setAtiva(false);

        contaRepository.save(conta);

        return toDTO(conta);

    }

    @Transactional
    public ContaResponseDTO ativarMinhaConta() {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmailWithLock(usuario.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        conta.setAtiva(true);

        contaRepository.save(conta);

        return toDTO(conta);

    }

    @Transactional
    public ContaResponseDTO desativar(Long id) {

        Usuario admin = usuarioAutenticadoService.getUsuarioLogado();

        definirVariaveisSessaoSql(admin.getId(), admin.getNome());

        Conta conta = contaRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id " + id));

        conta.setAtiva(false);

        Conta salva = contaRepository.save(conta);

        return toDTO(salva);

    }

    @Transactional
    public ContaResponseDTO ativar(Long id) {

        Usuario admin = usuarioAutenticadoService.getUsuarioLogado();

        definirVariaveisSessaoSql(admin.getId(), admin.getNome());

        Conta conta = contaRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id " + id));

        conta.setAtiva(true);

        Conta salva = contaRepository.save(conta);

        return toDTO(salva);

    }

    public ContaResponseDTO limite(LimiteRequestDTO dto) {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conta não encontrada para o usuário " + usuario.getEmail()));

        conta.setLimiteDiario(dto.limite());

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
        return new ContaResponseDTO(conta.getId(), conta.getUsuario().getNome(),
                conta.getUsuario().getEmail(), conta.getNumeroConta(), conta.getSaldo(), conta.getTipoConta(),
                conta.getAtiva(), conta.getDataCriacao(), conta.getLimiteDiario());

    }
}