package api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Documented
@Constraint(validatedBy = {}) 
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

@NotBlank(message = "O nome é obrigatório.")
@Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
@Pattern(regexp = "^[a-zA-ZáàâãéèêíïóôõöúçñÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇÑ\\s]+$", message = "O nome deve conter apenas letras e espaços.")
public @interface NomeValido {
    String message() default "Nome inválido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}