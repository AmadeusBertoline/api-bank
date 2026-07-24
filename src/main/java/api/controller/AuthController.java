package api.controller;

import api.dto.AdminRequestDTO;
import api.dto.LoginRequestDTO;
import api.dto.LoginResponseDTO;
import api.dto.UsuarioRequestDTO;
import api.enums.TipoRole;
import api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired

    @Operation(summary = "Registrar novo usuário", description = "cria um novo usuário")
    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(@RequestBody @Valid UsuarioRequestDTO dto) {
        return ResponseEntity.ok(authService.registrarUsuario(dto));
    }

    @PostMapping("/admin")
    public ResponseEntity<String> registrarAdmin(@Valid @RequestBody AdminRequestDTO dto) {
        return ResponseEntity.ok(authService.registrarAdmin(dto));
    }

    @Operation(summary = "Login — retorna JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}