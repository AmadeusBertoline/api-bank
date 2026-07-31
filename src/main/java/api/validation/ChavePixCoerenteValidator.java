package api.validation;

import java.util.regex.Pattern;
import api.dto.ChavePixRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChavePixCoerenteValidator implements ConstraintValidator<ChavePixCoerente, ChavePixRequestDTO> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("^\\d{14}$");
    private static final Pattern TELEFONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{10,14}$"); // Formato E.164 ou numérico
    private static final Pattern ALEATORIA_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"); // UUID

    @Override
    public boolean isValid(ChavePixRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getTipo() == null || dto.getChave() == null || dto.getChave().isBlank()) {
            return true; // Deixa as anotações de campo tratarem a obrigatoriedade
        }

        String chave = dto.getChave().trim();

        return switch (dto.getTipo()) {
            case CPF -> CPF_PATTERN.matcher(chave).matches();
            case CNPJ -> CNPJ_PATTERN.matcher(chave).matches();
            case EMAIL -> EMAIL_PATTERN.matcher(chave).matches();
            case TELEFONE -> TELEFONE_PATTERN.matcher(chave).matches();
            case ALEATORIA -> ALEATORIA_PATTERN.matcher(chave).matches();
        };
    }
}