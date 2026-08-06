package api.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;

public record PageResponseDTO<T>(
        List<T> conteudo,
        int paginaAtual,
        int tamanhoPagina,
        long totalElementos,
        int totalPaginas) implements Serializable {

    // converte o page recebido do retorno do repository convertido em DTO
    public PageResponseDTO(Page<T> page) {
        this(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
