package api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import api.dto.ContaResponseDTO;
import api.dto.UsuarioRequestDTO;
import api.enums.TipoRole;
import api.service.AuthService;
import api.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
@Tag(name = "tela admin")
public class AdminController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ContaService contaService;

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarAdmin(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(authService.registrarUsuario(dto, TipoRole.ROLE_ADMIN));
    }

    @Operation(summary = "desativar conta")
    @PatchMapping("/desativar-conta/{id}")
    public ResponseEntity<ContaResponseDTO> desativar(@PathVariable Long id) {

        ContaResponseDTO conta = contaService.desativar(id);

        return ResponseEntity.status(HttpStatus.OK).body(conta);

    }

    @Operation(summary = "ativar conta")
    @PatchMapping("/ativar-conta/{id}")
    public ResponseEntity<ContaResponseDTO> ativar(@PathVariable Long id) {

        ContaResponseDTO conta = contaService.ativar(id);

        return ResponseEntity.status(HttpStatus.OK).body(conta);

    }

    @Operation(summary = "listar todas as contas", description = "Um admin lista todas as contas criadas")
    @GetMapping("/listar-contas")
    public ResponseEntity<List<ContaResponseDTO>> listarTodas() {
        List<ContaResponseDTO> contas = contaService.listarTodas();
        return ResponseEntity.ok(contas);
    }

}
