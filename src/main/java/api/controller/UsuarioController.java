package api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import api.dto.EnderecoRequestDTO;
import api.dto.EnderecoResponseDTO;
import api.dto.UsuarioAtualizaEmailRequestDTO;
import api.dto.UsuarioResponseDTO;
import api.service.EnderecoService;
import api.service.UsuarioService;
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

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> meusDados() {
        UsuarioResponseDTO usuario = usuarioService.meusDados();
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    @PatchMapping("/endereco/atualizar")
    public ResponseEntity<EnderecoResponseDTO> atualizarEndereco(@RequestBody @Valid EnderecoRequestDTO dto) {

        EnderecoResponseDTO endereco = enderecoService.atualizar(dto);
        return ResponseEntity.status(HttpStatus.OK).body(endereco);

    }

    @PatchMapping("/email/atualizar")
    public ResponseEntity<UsuarioResponseDTO> atualizarEmail(@RequestBody @Valid UsuarioAtualizaEmailRequestDTO dto) {
        UsuarioResponseDTO usuario = usuarioService.atualizarEmail(dto);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

}
