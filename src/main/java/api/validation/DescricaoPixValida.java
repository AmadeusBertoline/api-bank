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

@Size(max = 140, message = "A descrição não pode exceder 140 caracteres.")
public @interface DescricaoPixValida {
    String message() default "Descrição em formato inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}