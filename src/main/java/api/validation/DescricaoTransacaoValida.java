package api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

@Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
public @interface DescricaoTransacaoValida {
    String message() default "Descrição em formato inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}