package api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

@NotNull(message = "Deve escolher um valor para modificar a conta: true ou false.")
public @interface StatusAtivoValido {
    String message() default "O status ativo/inativo da conta é obrigatório.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}