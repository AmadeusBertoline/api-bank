package api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import api.dto.ChavePixRequestDTO;
import api.dto.ChavePixResponseDTO;
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

        System.out.println(">>> CHAVE RECEBIDA: " + dto.getChave());
    System.out.println(">>> TIPO RECEBIDO: " + dto.getTipo());

        Usuario usuario = authService.buscarUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));

        ChavePix chavePix = new ChavePix();
        chavePix.setChave(dto.getChave());
        chavePix.setTipo(dto.getTipo());
        chavePix.setConta(conta);

        ChavePix salva = chavePixRepository.save(chavePix);

        return toDTO(salva);

    }

    public List<ChavePixResponseDTO> listarChavesPix(){

        return chavePixRepository.findAll()
        .stream()
        .map(this::toDTO)
        .collect(Collectors.toList());

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
