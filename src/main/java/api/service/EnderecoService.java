package api.service;

import org.springframework.stereotype.Service;

import api.dto.EnderecoResponseDTO;
import api.model.Endereco;

@Service
public class EnderecoService {

    public EnderecoResponseDTO toDTO(Endereco endereco) {
        
        if (endereco == null) {
            return null;
        }

        EnderecoResponseDTO dto = new EnderecoResponseDTO();
        dto.setId(endereco.getId());
        dto.setLogradouro(endereco.getLogradouro());
        dto.setNumero(endereco.getNumero());
        dto.setComplemento(endereco.getComplemento());
        dto.setBairro(endereco.getBairro());
        dto.setCidade(endereco.getCidade());
        dto.setUf(endereco.getUf());
        dto.setCep(endereco.getCep());

        return dto;
    }

}
