package api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "Exibir dados da minha conta corrente")
    @GetMapping("/me")
    public ResponseEntity<ContaResponseDTO> exibirContaCorrente() {

        ContaResponseDTO conta = contaService.meusDados();
        return ResponseEntity.ok(conta);
    }

}
