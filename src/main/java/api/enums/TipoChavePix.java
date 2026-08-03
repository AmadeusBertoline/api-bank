package api.enums;

import java.util.regex.Pattern;

public enum TipoChavePix {

    EMAIL("^[A-Za-z0-9+_.-]+@(.+)$"),
    CPF("^\\d{11}$"),
    CNPJ("^\\d{14}$"),
    TELEFONE("^\\+?[1-9]\\d{10,14}$"),
    ALEATORIA("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final Pattern pattern;

    TipoChavePix(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean isValido(String chave) {
        if (chave == null || chave.isBlank()) {
            return false;
        }
        return this.pattern.matcher(chave.trim()).matches();
    }

    public static TipoChavePix detectar(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("A chave Pix não pode ser vazia.");
        }

        String chaveLimpa = chave.trim();

        for (TipoChavePix tipo : values()) {
            if (tipo.isValido(chaveLimpa)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Formato de chave Pix não reconhecido.");
    }
}