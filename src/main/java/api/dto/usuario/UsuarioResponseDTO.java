package api.dto.usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

import api.dto.endereco.EnderecoResponseDTO;
import api.enums.TipoRole;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    String cpf,
    LocalDate dataNascimento,
    TipoRole role,
    LocalDateTime dataCriacao,
    EnderecoResponseDTO endereco
) {}