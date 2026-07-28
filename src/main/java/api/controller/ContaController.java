package api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import api.dto.ContaResponseDTO;
import api.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/contas")
@Tag(name = "Contas", description = "Gerenciamento de contas bancárias")
public class ContaController {
    @Autowired
    private ContaService contaService;

    @Operation(summary = "listar todas as contas", description = "Lista todas as contas criadas")
    @GetMapping("/all")
    public ResponseEntity<List<ContaResponseDTO>> listarTodas() {
        List<ContaResponseDTO> contas = contaService.listarTodas();
        return ResponseEntity.ok(contas);
    }

    @Operation(summary = "Exibir dados da minha conta corrente")
    @GetMapping("/me")
    public ResponseEntity<ContaResponseDTO> exibirContaCorrente() {

        ContaResponseDTO conta = contaService.meusDados();
        return ResponseEntity.ok(conta);
    }

    @Operation(summary = "desativar conta")
    @PatchMapping("/desativar/{id}")
    public ResponseEntity<ContaResponseDTO> desativar(@PathVariable Long id) {

        ContaResponseDTO conta = contaService.desativar(id);

        return ResponseEntity.status(HttpStatus.OK).body(conta);

    }

    @Operation(summary = "ativar conta")
    @PatchMapping("/ativar/{id}")
    public ResponseEntity<ContaResponseDTO> ativar(@PathVariable Long id) {

        ContaResponseDTO conta = contaService.ativar(id);

        return ResponseEntity.status(HttpStatus.OK).body(conta);

    }

    @Operation(summary = "deletar conta")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {

        contaService.deletar(id);
        return ResponseEntity.noContent().build();

    }
}
