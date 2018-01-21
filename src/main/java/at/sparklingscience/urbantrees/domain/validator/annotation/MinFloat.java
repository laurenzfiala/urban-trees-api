package at.sparklingscience.urbantrees.domain.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import at.sparklingscience.urbantrees.domain.validator.MinFloatValidator;

/**
 * Validation annotation to validate floats for their minimum value.
 * 
 * @author Laurenz Fiala
 * @since 2018/01/21
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { MinFloatValidator.class })
public @interface MinFloat {

	String message() default "must be greater than or equal to {value}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * The value to use for validation (= minimum allowed value).
	 */
	float value();

}
