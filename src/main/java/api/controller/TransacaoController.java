package api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import api.dto.PixRequestDTO;
import api.dto.TransacaoResponseDTO;
import api.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/transacoes")
@Tag(name = "Transações", description = "Operações financeiras: depósito, saque e transferência")
public class TransacaoController {

    @Autowired
    private TransacaoService transacaoService;

    @Operation(summary = "realizar PIX", description = "realizar um PIX")
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> pix(@RequestBody @Valid PixRequestDTO dto) {

        TransacaoResponseDTO realizada = transacaoService.pix(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(realizada);

    }

    @PageableAsQueryParam
    @Operation(summary = "Extrato da conta", description = "Lista todas as transações da conta, da mais recente para a mais antiga")
    @GetMapping("/extrato")
    public ResponseEntity<Page<TransacaoResponseDTO>> listarPorConta(
            @Parameter(hidden = true) @PageableDefault(page = 0, size = 10, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TransacaoResponseDTO> transacoes = transacaoService.listarPorConta(pageable);
        return ResponseEntity.ok(transacoes);
    }

}
