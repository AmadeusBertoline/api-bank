package api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.dto.EnderecoRequestDTO;
import api.dto.EnderecoResponseDTO;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.EnderecoRepository;

@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private UsuarioAutenticadoService usuarioAutenticadoService;

    public EnderecoResponseDTO atualizar(EnderecoRequestDTO dto) {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Endereco endereco = enderecoRepository.findById(usuario.getEndereco().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado de id " + usuario.getEndereco().getId()));

        if (!usuario.getId().equals(endereco.getUsuario().getId())) {
            throw new RegraNegocioException("Um usuário só pode alterar o próprio endereço");
        }

        endereco.setBairro(dto.getBairro());
        endereco.setCep(dto.getCep());
        endereco.setCidade(dto.getCidade());
        endereco.setComplemento(dto.getComplemento());
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setUf(dto.getUf());

        Endereco salvo = enderecoRepository.save(endereco);

        return toDTO(salvo);
    }

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