package api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import api.dto.page.PageResponseDTO;
import api.dto.pix.PixRequestDTO;
import api.dto.transacao.ArquivoDownloadResponseDTO;
import api.dto.transacao.ArquivoDownloadRequestDTO;
import api.dto.transacao.TransacaoResponseDTO;
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

    @Operation(summary = "Realizar PIX", description = "Realiza um PIX")
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> pix(@RequestBody @Valid PixRequestDTO dto) {

        TransacaoResponseDTO realizada = transacaoService.pix(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(realizada);

    }

    @PageableAsQueryParam
    @Operation(summary = "Extrato da conta", description = "Lista todas as transações da conta, da mais recente para a mais antiga")
    @GetMapping("/extrato")
    public ResponseEntity<PageResponseDTO<TransacaoResponseDTO>> listarPorConta(
            @Parameter(hidden = true) @PageableDefault(page = 0, size = 10, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponseDTO<TransacaoResponseDTO> transacoes = transacaoService.extrato(pageable);
        return ResponseEntity.ok(transacoes);
    }

    @Operation(summary = "Baixar extrato", description = "Baixa extrato de transações")
    @GetMapping("/extrato/download")
    public ResponseEntity<byte[]> downloadExtratoCsv(@ParameterObject @Valid ArquivoDownloadRequestDTO filtro) {
        ArquivoDownloadResponseDTO arquivo = transacaoService.gerarCsvExtrato(filtro);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo.nomeArquivo() + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(arquivo.conteudo());
    }

}
