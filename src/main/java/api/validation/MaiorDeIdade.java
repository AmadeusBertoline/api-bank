package api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaiorDeIdadeValidator.class)
@Documented
public @interface MaiorDeIdade {

    String message() default "A pessoa deve ter no mínimo 18 anos";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
