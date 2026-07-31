package api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

@NotNull(message = "O valor da transação é obrigatório.")
@Positive(message = "O valor da transação deve ser maior que zero.")
public @interface ValorTransacaoValido {
    String message() default "Valor de transação inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}