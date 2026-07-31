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

@NotBlank(message = "A chave Pix é obrigatória.")
@Size(min = 5, max = 77, message = "A chave Pix deve ter entre 5 e 77 caracteres.")
public @interface ChavePixValida {
    String message() default "Chave Pix em formato inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}