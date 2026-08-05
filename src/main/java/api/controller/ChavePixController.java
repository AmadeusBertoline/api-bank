package api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import api.dto.ChavePixRequestDTO;
import api.dto.ChavePixResponseDTO;
import api.service.ChavePixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Chaves Pix")
@RestController
@RequestMapping("/chaves")
public class ChavePixController {

    @Autowired
    private ChavePixService chavePixService;

    @Operation(summary = "Cadastrar chave pix", description = "Cadastra chave pix de diversos tipos")
    @PostMapping
    public ResponseEntity<ChavePixResponseDTO> cadastrar(@RequestBody @Valid ChavePixRequestDTO dto) {
        ChavePixResponseDTO chaveCriada = chavePixService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(chaveCriada);
    }

    @GetMapping
    public ResponseEntity<List<ChavePixResponseDTO>> listarChavesPix() {
        List<ChavePixResponseDTO> lista = chavePixService.listarChavesPix();
        return ResponseEntity.status(HttpStatus.OK).body(lista);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {

        chavePixService.deletar(id);
        return ResponseEntity.noContent().build();

    }

}
