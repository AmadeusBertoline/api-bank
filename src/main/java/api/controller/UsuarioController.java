package api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dto.endereco.EnderecoRequestDTO;
import api.dto.endereco.EnderecoResponseDTO;
import api.dto.usuario.UsuarioAtualizaEmailRequestDTO;
import api.dto.usuario.UsuarioAtualizaSenhaRequestDTO;
import api.dto.usuario.UsuarioResponseDTO;
import api.service.EnderecoService;
import api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Operações de consulta de dados e atualizações")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EnderecoService enderecoService;

    @Operation(summary = "Exibir dados do meu usuário", description = "Exibe meus dados de usuário")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> meusDados() {
        UsuarioResponseDTO usuario = usuarioService.meusDados();
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Atualizar meu endereço", description = "Atualiza qualquer informação do meu endereço")
    @PatchMapping("/endereco/atualizar")
    public ResponseEntity<EnderecoResponseDTO> atualizarEndereco(@RequestBody @Valid EnderecoRequestDTO dto) {

        EnderecoResponseDTO endereco = enderecoService.atualizar(dto);
        return ResponseEntity.status(HttpStatus.OK).body(endereco);

    }

    @Operation(summary = "Atualizar meu email", description = "Atualiza meu email (login)")
    @PatchMapping("/email/atualizar")
    public ResponseEntity<UsuarioResponseDTO> atualizarEmail(@RequestBody @Valid UsuarioAtualizaEmailRequestDTO dto) {
        UsuarioResponseDTO usuario = usuarioService.atualizarEmail(dto);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @Operation(summary = "Atualizar minha senha", description = "Atualiza minha senha (login)")
    @PatchMapping("/senha/atualizar")
    public ResponseEntity<UsuarioResponseDTO> atualizarSenha(@RequestBody @Valid UsuarioAtualizaSenhaRequestDTO dto) {

        UsuarioResponseDTO usuario = usuarioService.atualizarSenha(dto);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);

    }

}
