package api.service;

import org.springframework.stereotype.Service;
import api.dto.EnderecoRequestDTO;
import api.dto.EnderecoResponseDTO;
import api.enums.StatusConta;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.EnderecoRepository;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public EnderecoService(
            EnderecoRepository enderecoRepository,
            UsuarioAutenticadoService usuarioAutenticadoService) {

        this.enderecoRepository = enderecoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    public EnderecoResponseDTO atualizar(EnderecoRequestDTO dto) {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        if (usuario.getConta().getStatus().equals(StatusConta.BLOQUEADA)) {

            throw new RegraNegocioException(
                    "Sua conta está bloqueada, você não pode realizar transações nem alterações");

        }

        Endereco endereco = enderecoRepository.findById(usuario.getEndereco().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Endereço não encontrado de id " + usuario.getEndereco().getId()));

        if (!usuario.getId().equals(endereco.getUsuario().getId())) {
            throw new RegraNegocioException("Um usuário só pode alterar o próprio endereço");
        }

        endereco.setBairro(dto.bairro());
        endereco.setCep(dto.cep());
        endereco.setCidade(dto.cidade());
        endereco.setComplemento(dto.complemento());
        endereco.setLogradouro(dto.logradouro());
        endereco.setNumero(dto.numero());
        endereco.setUf(dto.uf());

        Endereco salvo = enderecoRepository.save(endereco);

        return toDTO(salvo);
    }

    public EnderecoResponseDTO toDTO(Endereco endereco) {

        if (endereco == null) {
            return null;
        }

        return new EnderecoResponseDTO(endereco.getId(), endereco.getLogradouro(),
                endereco.getNumero(), endereco.getComplemento(), endereco.getBairro(), endereco.getCidade(),
                endereco.getUf(), endereco.getCep());

    }
}