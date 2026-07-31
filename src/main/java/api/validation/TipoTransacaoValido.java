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

@NotNull(message = "O tipo da transação é obrigatório (DEPOSITO, SAQUE, TRANSFERENCIA).")
public @interface TipoTransacaoValido {
    String message() default "Tipo de transação inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}