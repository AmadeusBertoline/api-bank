package api.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import api.dto.TransacaoRequestDTO;
import api.dto.TransacaoResponseDTO;
import api.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/transacoes")
@Tag(name = "Transações",  description = "Operações financeiras: depósito, saque e transferência")
public class TransacaoController {
    
    @Autowired
    private TransacaoService transacaoService;

    @Operation(summary = "realizar transação", description = "Executa a transação escolhida pelo cliente: depósito, saque e transferência")
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> realizarTransacao(@RequestBody @Valid TransacaoRequestDTO dto){

        TransacaoResponseDTO realizada = transacaoService.realizarTransacao(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(realizada);
        
    }

    @Operation(summary = "extrato da conta", description = "Lista todas as transações da conta, da mais recente para a mais antiga")
    @GetMapping("/conta/{contaId}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorConta(@PathVariable Long contaId){

        List<TransacaoResponseDTO> transacoes = new ArrayList<>();
        transacoes = transacaoService.listarPorConta(contaId);
        return ResponseEntity.ok(transacoes);

    }

}
