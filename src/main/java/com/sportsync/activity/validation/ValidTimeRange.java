package com.sportsync.activity.validation;

import com.sportsync.activity.ActivityCreateDto;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StartBeforeEndValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeRange {
    String message() default "End time must be after start time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class StartBeforeEndValidator implements ConstraintValidator<ValidTimeRange, ActivityCreateDto> {
    @Override
    public boolean isValid(ActivityCreateDto dto, ConstraintValidatorContext context) {
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            return true;
        }
        return dto.getStartTime().isBefore(dto.getEndTime());
    }
}
