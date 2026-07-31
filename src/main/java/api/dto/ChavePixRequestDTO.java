package api.dto;

import api.enums.TipoChavePix;
import api.validation.ChavePixCoerente;
import api.validation.ChavePixValida;
import api.validation.TipoChavePixValido;

@ChavePixCoerente
public class ChavePixRequestDTO {

    @ChavePixValida
    private String chave;

    @TipoChavePixValido
    private TipoChavePix tipo;

    // Getters e Setters
    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public TipoChavePix getTipo() {
        return tipo;
    }

    public void setTipo(TipoChavePix tipo) {
        this.tipo = tipo;
    }
}
