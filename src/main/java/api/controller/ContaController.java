package api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dto.ContaRequestDTO;
import api.dto.ContaResponseDTO;
import api.service.ContaService;

@RestController
@RequestMapping("/contas")
public class ContaController {
    @Autowired
    private ContaService contaService;

    @PostMapping
    public ResponseEntity<ContaResponseDTO> criar(@RequestBody ContaRequestDTO dto) {
        ContaResponseDTO resposta = contaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public ResponseEntity<List<ContaResponseDTO>> listarTodas() {
        List<ContaResponseDTO> contas = contaService.listarTodas();
        return ResponseEntity.ok(contas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponseDTO> buscaPorId(@PathVariable Long id){

        ContaResponseDTO conta = contaService.buscarPorId(id);
        return ResponseEntity.ok(conta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaResponseDTO> atualizar(@PathVariable Long id, @RequestBody ContaRequestDTO dto){

        ContaResponseDTO conta = contaService.atualizar(id, dto);
        return ResponseEntity.ok(conta);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){

        contaService.deletar(id);
        return ResponseEntity.noContent().build();

    }
}
