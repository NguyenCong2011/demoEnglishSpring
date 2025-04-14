package com.example.english.demo.validator;

//này dùng custom giống như các cái annotation khác như kiểu Size
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD })//chỉ sử dụng cho field còn method và các cái khsac không cần
@Retention(RUNTIME)//sử dụng khi runtinme
@Constraint(validatedBy = {DobValidator.class})//class chịu trach nhiệm
public @interface DobConstraint {
    String message() default "Invalid date of birth";

    int min();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
