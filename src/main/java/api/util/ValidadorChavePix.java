package api.util;

import api.enums.TipoChavePix;

public class ValidadorChavePix {

    // Regex para Chave Aleatória / EVP (UUID de 36 caracteres)
    private static final String REGEX_UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    // Regex para E-mail
    private static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";

    public static TipoChavePix identificarTipo(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("A chave Pix não pode ser vazia.");
        }

        String chaveLimpa = chave.trim();

        // 1. E-mail (Presença de @ e formato de e-mail)
        if (chaveLimpa.matches(REGEX_EMAIL)) {
            return TipoChavePix.EMAIL;
        }

        // 2. Chave Aleatória / EVP (Padrão UUID)
        if (chaveLimpa.matches(REGEX_UUID)) {
            return TipoChavePix.ALEATORIA;
        }

        // 3. Telefone no Padrão Internacional BACEN (Inicia com +55...)
        if (chaveLimpa.startsWith("+")) {
            return TipoChavePix.PHONE;
        }

        // Extrai apenas os números para validar CPF, CNPJ e telefones sem máscara
        String apenasNumeros = chaveLimpa.replaceAll("\\D", "");

        // 4. CNPJ (14 dígitos numéricos)
        if (apenasNumeros.length() == 14) {
            return TipoChavePix.CNPJ;
        }

        // 5. CPF (11 dígitos numéricos)
        if (apenasNumeros.length() == 11) {
            return TipoChavePix.CPF;
        }

        // 6. Telefone sem o DDI +55 (10 ou 11 dígitos se não caiu como CPF)
        if (apenasNumeros.length() == 10) {
            return TipoChavePix.PHONE;
        }

        throw new IllegalArgumentException("Formato de chave Pix inválido.");
    }
}
