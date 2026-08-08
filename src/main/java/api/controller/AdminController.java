package api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.converters.models.PageableAsQueryParam;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin")
public class AdminController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ContaService contaService;

    @Operation(summary = "Registar um novo admin", description = "Um admin registra outro admin")
    @PostMapping("/registrar")
    public ResponseEntity<String> registrarAdmin(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(authService.registrarUsuario(dto, TipoRole.ROLE_ADMIN));
    }

    @Operation(summary = "Bloquear conta", description = "Um admin bloqueia uma conta")
    @PatchMapping("/bloquear-conta/{id}")
    public ResponseEntity<ContaResponseDTO> bloquear(@PathVariable Long id) {

        ContaResponseDTO conta = contaService.bloquear(id);

        return ResponseEntity.status(HttpStatus.OK).body(conta);

    }

    @Operation(summary = "Desbloquear conta", description = "Um admin desbloqueia uma conta")
    @PatchMapping("/desbloquear-conta/{id}")
    public ResponseEntity<ContaResponseDTO> desbloquear(@PathVariable Long id) {

        ContaResponseDTO conta = contaService.desbloquear(id);

        return ResponseEntity.status(HttpStatus.OK).body(conta);

    }

    @PageableAsQueryParam
    @Operation(summary = "Listar todas as contas", description = "Um admin lista todas as contas criadas")
    @GetMapping("/listar-contas")
    public ResponseEntity<Page<ContaResponseDTO>> listarTodas(
            @Parameter(hidden = true) @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ContaResponseDTO> contas = contaService.listarTodas(pageable);
        return ResponseEntity.ok(contas);
    }

}
