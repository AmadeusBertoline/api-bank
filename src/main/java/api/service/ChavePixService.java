package api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import api.dto.chavePix.ChavePixRequestDTO;
import api.dto.chavePix.ChavePixResponseDTO;
import api.enums.StatusConta;
import api.enums.TipoChavePix;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.ChavePix;
import api.model.Conta;
import api.model.Usuario;
import api.repository.ChavePixRepository;
import api.repository.ContaRepository;
import jakarta.transaction.Transactional;

@Service
public class ChavePixService {

    private final ChavePixRepository chavePixRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ContaRepository contaRepository;

    public ChavePixService(
            ChavePixRepository chavePixRepository,
            UsuarioAutenticadoService usuarioAutenticadoService,
            ContaRepository contaRepository) {

        this.chavePixRepository = chavePixRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.contaRepository = contaRepository;
    };

    @Transactional
    public ChavePixResponseDTO cadastrar(ChavePixRequestDTO dto) {

        if (chavePixRepository.findByChave(dto.chave()).isPresent()) {
            throw new RegraNegocioException("Chave pix já cadastrada");
        }

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        if (usuario.getConta().getStatus().equals(StatusConta.BLOQUEADA)) {

            throw new RegraNegocioException(
                    "Sua conta está bloqueada, você não pode realizar transações nem alterações de chave pix");

        }

        Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        ChavePix chavePix = new ChavePix();

        TipoChavePix tipo = TipoChavePix.detectar(dto.chave());

        if (tipo.equals(TipoChavePix.CPF) && !dto.chave().equals(usuario.getCpf())) {
            throw new RegraNegocioException("Você não pode usar o CPF de terceiros");
        }

        chavePix.setChave(dto.chave());
        chavePix.setTipo(tipo);
        chavePix.setConta(conta);

        ChavePix salva = chavePixRepository.save(chavePix);

        return toDTO(salva);

    }

    public List<ChavePixResponseDTO> listarChavesPix() {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Usuário não encontrado de email " + usuario.getEmail()));

        return chavePixRepository.findAllByContaId(conta.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

    }

    public void deletar(Long id) {

        ChavePix chavePix = chavePixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chave pix não encontrada de id " + id));

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        if (usuario.getConta().getStatus().equals(StatusConta.BLOQUEADA)) {

            throw new RegraNegocioException(
                    "Sua conta está bloqueada, você não pode realizar transações nem alterações de chave pix");

        }

        if (!chavePix.getConta().getUsuario().getId().equals(usuario.getId())) {
            throw new RegraNegocioException("Somente a conta dona da chave pode altera-la");
        }

        chavePixRepository.delete(chavePix);

    }

    private ChavePixResponseDTO toDTO(ChavePix chavePix) {

        return new ChavePixResponseDTO(chavePix.getId(), chavePix.getChave(), chavePix.getTipo(),
                chavePix.getConta().getId());

    }

}