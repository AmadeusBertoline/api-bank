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

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

@NotBlank(message = "O CEP é obrigatório.")
@Pattern(regexp = "\\d{8}|\\d{5}-\\d{3}", message = "O CEP deve estar no formato 12345678 ou 12345-678.")
public @interface CepValido {
    String message() default "CEP em formato inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}