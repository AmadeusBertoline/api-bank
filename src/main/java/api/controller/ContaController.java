package api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dto.conta.ContaResponseDTO;
import api.dto.limite.LimiteRequestDTO;
import api.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/contas")
@Tag(name = "Contas", description = "Gerenciamento de contas bancárias")
public class ContaController {
    @Autowired
    private ContaService contaService;

    @Operation(summary = "Exibir dados da minha conta corrente", description = "Exibe dados da minha conta corrente")
    @GetMapping("/me")
    public ResponseEntity<ContaResponseDTO> exibirContaCorrente() {

        ContaResponseDTO conta = contaService.meusDados();
        return ResponseEntity.ok(conta);
    }

    @Operation(summary = "Encerrar minha conta", description = "Encerra minha conta")
    @PatchMapping("/encerrar")
    public ResponseEntity<ContaResponseDTO> encerrar() {
        ContaResponseDTO conta = contaService.encerrar();
        return ResponseEntity.status(HttpStatus.OK).body(conta);
    }

    @Operation(summary = "Ajustar limite diário da minha conta", description = "Muda o limite de pix que eu realizo")
    @PatchMapping("/limite")
    public ResponseEntity<ContaResponseDTO> limite(LimiteRequestDTO dto) {

        ContaResponseDTO conta = contaService.limite(dto);
        return ResponseEntity.status(HttpStatus.OK).body(conta);

    }

}
