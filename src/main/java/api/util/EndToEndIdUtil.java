package api.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class EndToEndIdUtil {

    // ISPB fictício de 8 dígitos para a sua instituição/fintech
    private static final String ISPB_INSTITUICAO = "12345678";

    public static String gerar() {
        // Data e Hora em UTC no formato AAAAMMDDHHMM (12 caracteres)
        String dataHoraUtc = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        // 14 caracteres alfanuméricos aleatórios extraídos de um UUID
        String aleatorio = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 14);

        // 'E' + 8 dígitos + 12 dígitos + 14 caracteres = 35 caracteres
        return "E" + ISPB_INSTITUICAO + dataHoraUtc + aleatorio;
    }
}