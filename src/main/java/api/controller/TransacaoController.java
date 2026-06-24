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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {
    
    @Autowired
    private TransacaoService transacaoService;

    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> realizarTransacao(@RequestBody @Valid TransacaoRequestDTO dto){

        TransacaoResponseDTO realizada = transacaoService.realizarTransacao(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(realizada);
        
    }

    @GetMapping("/conta/{contaId}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorConta(@PathVariable Long contaId){

        List<TransacaoResponseDTO> transacoes = new ArrayList<>();
        transacoes = transacaoService.listarPorConta(contaId);
        return ResponseEntity.ok(transacoes);

    }

}
