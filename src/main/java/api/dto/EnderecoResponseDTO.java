package api.dto;

import lombok.Data;

@Data
public class EnderecoResponseDTO {

    private Long id;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;
    
}
