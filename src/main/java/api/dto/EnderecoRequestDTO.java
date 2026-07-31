package api.dto;

import api.validation.BairroValido;
import api.validation.CepValido;
import api.validation.CidadeValida;
import api.validation.LogradouroValido;
import api.validation.NumeroEnderecoValido;
import api.validation.UfValida;
import lombok.Data;

@Data
public class EnderecoRequestDTO {

    @LogradouroValido
    private String logradouro;

    @NumeroEnderecoValido
    private String numero;

    private String complemento;

    @BairroValido
    private String bairro;

    @CidadeValida
    private String cidade;

    @UfValida
    private String uf;

    @CepValido
    private String cep;
}
