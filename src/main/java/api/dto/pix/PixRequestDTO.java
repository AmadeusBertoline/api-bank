package api.dto.pix;

import java.math.BigDecimal;
import api.validation.ChavePixValida;
import api.validation.DescricaoPixValida;
import api.validation.ValorPositivoValido;

public record PixRequestDTO(
    @ChavePixValida String chavePix,
    @ValorPositivoValido BigDecimal valor,
    @DescricaoPixValida String descricao
) {}