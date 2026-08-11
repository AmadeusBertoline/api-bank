package api.dto.usuario;

import api.validation.SenhaValida;

public record UsuarioAtualizaSenhaRequestDTO(

        @SenhaValida String senhaAtual,

        @SenhaValida String senhaNova,

        @SenhaValida String senhaRepetida

) {

}
