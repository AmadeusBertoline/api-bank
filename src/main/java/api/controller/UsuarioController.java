package api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dto.UsuarioResponseDTO;
import api.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Operações de consulta de dados e atualizações")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> meusDados(){
        UsuarioResponseDTO usuario = usuarioService.meusDados();
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }
    
    //fazer atualização do endereço

}
