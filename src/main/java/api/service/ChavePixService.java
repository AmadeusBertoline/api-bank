package api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import api.dto.ChavePixRequestDTO;
import api.dto.ChavePixResponseDTO;
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

    @Autowired
    private ChavePixRepository chavePixRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ContaRepository contaRepository;

    @Transactional
    public ChavePixResponseDTO cadastrar(ChavePixRequestDTO dto) {

        Usuario usuario = authService.buscarUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        

        ChavePix chavePix = new ChavePix();

        if(dto.getTipo().equals(TipoChavePix.CPF)){
            chavePix.setChave(usuario.getCpf());
        }else{
            chavePix.setChave(dto.getChave());
        }

        chavePix.setTipo(dto.getTipo());
        chavePix.setConta(conta);

        ChavePix salva = chavePixRepository.save(chavePix);

        return toDTO(salva);

    }

    public List<ChavePixResponseDTO> listarChavesPix() {

        Usuario usuario = authService.buscarUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Usuário não encontrado de email " + usuario.getEmail()));

        return chavePixRepository.findAllByContaId(conta.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

    }

    public ChavePixResponseDTO atualizar(Long id, ChavePixRequestDTO dto) {

        Usuario usuario = authService.buscarUsuarioLogado();

        Long usuarioDono = chavePixRepository.findUsuarioIdByIdDaChave(id);

        if (!(usuario.getId() == usuarioDono)) {
            throw new RegraNegocioException("Somente a conta dona da chave pode altera-la");
        }

        ChavePix chavePix = chavePixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chave pix não encontrada de id " + id));

        chavePix.setChave(dto.getChave());
        chavePix.setTipo(dto.getTipo());

        ChavePix salva = chavePixRepository.save(chavePix);

        return toDTO(salva);

    }

    public String deletar(Long id) {

        Usuario usuario = authService.buscarUsuarioLogado();

        Long usuarioDono = chavePixRepository.findUsuarioIdByIdDaChave(id);

        if (!(usuario.getId() == usuarioDono)) {
            throw new RegraNegocioException("Somente a conta dona da chave pode altera-la");
        }

        chavePixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chave pix não encontrada de id " + id));

        chavePixRepository.deleteById(id);

        return "Chave deletada";

    }

    private ChavePixResponseDTO toDTO(ChavePix chavePix) {

        ChavePixResponseDTO dto = new ChavePixResponseDTO();
        dto.setId(chavePix.getId());
        dto.setChave(chavePix.getChave());
        dto.setTipo(chavePix.getTipo());

        if (chavePix.getConta() != null) {
            dto.setContaId(chavePix.getConta().getId());
        }

        return dto;
    }

}
