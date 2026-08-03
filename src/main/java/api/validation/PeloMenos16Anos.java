package api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

@Documented
@Constraint(validatedBy = PeloMenos16AnosValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)

@NotNull(message = "A data de nascimento é obrigatória.")
@Past(message = "A data de nascimento deve ser uma data no passado.")
public @interface PeloMenos16Anos {
    String message() default "O usuário deve ter pelo menos 16 anos para se cadastrar.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}