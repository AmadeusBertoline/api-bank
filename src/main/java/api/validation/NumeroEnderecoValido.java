package api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

@NotBlank(message = "O número do endereço é obrigatório.")
@Size(max = 20, message = "O número deve ter no máximo 20 caracteres.")
public @interface NumeroEnderecoValido {
    String message() default "Número de endereço inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}