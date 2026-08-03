package api.validation;

import api.enums.TipoChavePix;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChavePixValidator implements ConstraintValidator<ChavePixValida, String> {

    @Override
    public boolean isValid(String chave, ConstraintValidatorContext context) {
        if (chave == null || chave.isBlank()) {
            return true; // Deixa o @NotBlank tratar a obrigatoriedade
        }

        // Tenta detectar se o texto bate com qualquer um dos tipos do Enum
        try {
            TipoChavePix.detectar(chave);
            return true; // É uma chave válida!
        } catch (IllegalArgumentException e) {
            return false; // Formato não reconhecido (inválido)
        }
    }
}