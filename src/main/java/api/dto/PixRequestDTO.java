package api.dto;

import java.math.BigDecimal;
import api.validation.ChavePixValida;
import api.validation.DescricaoPixValida;
import api.validation.ValorPositivoValido;
import lombok.Data;

@Data
public class PixRequestDTO {

    @ChavePixValida
    private String chavePix;

    @ValorPositivoValido
    private BigDecimal valor;

    @DescricaoPixValida
    private String descricao;
}