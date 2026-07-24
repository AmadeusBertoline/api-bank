package api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EnderecoRequestDTO {

    @NotBlank(message = "O logradouro é obrigatório")
    private String logradouro;

    @NotBlank(message = "O número é obrigatório")
    private String numero;

    private String complemento;

    @NotBlank(message = "O bairro é obrigatório")
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória")
    private String cidade;

    @NotBlank(message = "O UF é obrigatório")
    @Pattern(regexp = "[A-Z]{2}", message = "O UF deve conter exatamente 2 letras maiúsculas (ex: SP, RJ)")
    private String uf;

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(regexp = "\\d{8}|\\d{5}-\\d{3}", message = "O CEP deve estar no formato 12345678 ou 12345-678")
    private String cep;
}
