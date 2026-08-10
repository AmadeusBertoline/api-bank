package api.dto.transacao;

public record ArquivoDownloadResponseDTO(
    byte[] conteudo,
    String nomeArquivo
) {}