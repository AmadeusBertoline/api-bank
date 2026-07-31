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

@NotBlank(message = "A UF é obrigatória.")
@Pattern(regexp = "[A-Z]{2}", message = "A UF deve conter exatamente 2 letras maiúsculas (ex: SP, RJ).")
public @interface UfValida {
    String message() default "UF inválida.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}