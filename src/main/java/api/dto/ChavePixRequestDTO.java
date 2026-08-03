package api.dto;

import api.validation.ChavePixValida;
import jakarta.validation.constraints.NotBlank;

public class ChavePixRequestDTO {

    @NotBlank(message = "A chave Pix é obrigatória.")
    @ChavePixValida
    private String chave;

    // Getters e Setters
    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

}
