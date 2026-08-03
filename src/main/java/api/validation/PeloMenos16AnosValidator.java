package api.validation;

import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PeloMenos16AnosValidator implements ConstraintValidator<PeloMenos16Anos, LocalDate> {

    @Override
    public boolean isValid(LocalDate dataNascimento, ConstraintValidatorContext context) {
        if (dataNascimento == null) {
            return true; 
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears() >= 16;
    }
}