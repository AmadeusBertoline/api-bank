package api.dto;

import api.validation.BairroValido;
import api.validation.CepValido;
import api.validation.CidadeValida;
import api.validation.LogradouroValido;
import api.validation.NumeroEnderecoValido;
import api.validation.UfValida;

public record EnderecoRequestDTO(
    @LogradouroValido String logradouro,
    @NumeroEnderecoValido String numero,
    String complemento,
    @BairroValido String bairro,
    @CidadeValida String cidade,
    @UfValida String uf,
    @CepValido String cep
) {

    public String getUf() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUf'");
    }}