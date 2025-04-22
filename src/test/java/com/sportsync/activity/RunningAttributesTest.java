package com.sportsync.activity;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RunningAttributesTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldThrowWhenNegativeDistance() {
        RunningAttributes attrs =
                RunningAttributes.builder()
                        .pace(5.0)
                        .distance(-10.0)
                        .terrain(RunningTerrain.ASPHALT)
                        .build();

        var violations = validator.validate(attrs);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("distance")));
    }
}
