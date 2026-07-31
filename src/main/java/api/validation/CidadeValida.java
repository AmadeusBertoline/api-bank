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

@NotBlank(message = "A cidade é obrigatória.")
@Size(min = 2, max = 100, message = "A cidade deve ter entre 2 e 100 caracteres.")
public @interface CidadeValida {
    String message() default "Cidade inválida.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}