package api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.dto.AtualizarContaRequestDTO;
import api.dto.ContaRequestDTO;
import api.dto.ContaResponseDTO;
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

    public ContaResponseDTO criar(ContaRequestDTO dto){

        Usuario usuario = authService.buscarUsuarioLogado();

        Conta conta = new Conta();
        conta.setUsuario(usuario);
        conta.setNumeroConta(dto.getNumeroConta());
        conta.setSaldo(dto.getSaldo());
        conta.setTipoConta(dto.getTipoConta());

        Conta salva = contaRepository.save(conta);

        return toDTO(salva);
        
    }

    public List<ContaResponseDTO> listarTodas(){
        return contaRepository.findAll()
        .stream()
        .map(this::toDTO)
        .collect(Collectors.toList());

    }

    public ContaResponseDTO buscarPorId(Long id){

        Conta conta = contaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id: " +id));

        return toDTO(conta);

    }

    public ContaResponseDTO atualizar(Long id, AtualizarContaRequestDTO dto){

        Conta conta = contaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id: " +id));

        conta.setTipoConta(dto.getTipoConta());

        Conta atualizada = contaRepository.save(conta);

        return toDTO(atualizada);

    }

    public void deletar(Long id){

        Conta conta = contaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id: " +id));

        contaRepository.delete(conta);

    }

    private ContaResponseDTO toDTO(Conta conta){
        ContaResponseDTO dto = new ContaResponseDTO();
        dto.setId(conta.getId());
        dto.setUsuario(conta.getUsuario());
        dto.setNumeroConta(conta.getNumeroConta());
        dto.setSaldo(conta.getSaldo());
        dto.setTipoConta(conta.getTipoConta());
        dto.setAtiva(conta.getAtiva());
        dto.setDataCriacao(conta.getDataCriacao());
        return dto;
    }
}