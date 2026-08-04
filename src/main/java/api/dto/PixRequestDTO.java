package api.dto;

import java.math.BigDecimal;
import api.validation.ChavePixValida;
import api.validation.DescricaoPixValida;
import api.validation.ValorPositivoValido;

public record PixRequestDTO(
    @ChavePixValida String chavePix,
    @ValorPositivoValido BigDecimal valor,
    @DescricaoPixValida String descricao
) {

    public void setValor(BigDecimal bigDecimal) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setValor'");
    }}