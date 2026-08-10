package api.dto.transacao;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;

public record ArquivoDownloadRequestDTO(
    @Schema(
        description = "Data inicial do extrato (Padrão: 30 dias atrás)", 
        example = "2026-07-11", 
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataInicio,

    @Schema(
        description = "Data final do extrato (Padrão: hoje)", 
        example = "2026-08-10", 
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataFim
) {
    public ArquivoDownloadRequestDTO {
        if (dataFim == null) {
            dataFim = LocalDate.now();
        }
        if (dataInicio == null) {
            dataInicio = dataFim.minusDays(30);
        }
    }
}