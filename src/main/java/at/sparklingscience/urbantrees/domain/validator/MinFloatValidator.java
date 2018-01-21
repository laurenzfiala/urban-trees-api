package at.sparklingscience.urbantrees.domain.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import at.sparklingscience.urbantrees.domain.validator.annotation.MinFloat;

/**
 * Validates the given float by whether its value is greater than or equal
 * to the value specified.
 * 
 * @see MinFloat
 * @author Laurenz Fiala
 * @since 2018/01/21
 */
public class MinFloatValidator implements ConstraintValidator<MinFloat, Float> {

	private Float minValue;

	@Override
	public void initialize(MinFloat annotation) {
		this.minValue = annotation.value();
	}

	@Override
	public boolean isValid(Float validationValue, ConstraintValidatorContext validationContext) {
		return validationValue >= this.minValue;
	}
	
}
