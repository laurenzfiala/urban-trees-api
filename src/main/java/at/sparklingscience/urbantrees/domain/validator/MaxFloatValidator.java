package at.sparklingscience.urbantrees.domain.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import at.sparklingscience.urbantrees.domain.validator.annotation.MaxFloat;

/**
 * Validates the given float by whether its value is less than or equal
 * to the value specified.
 * 
 * @see MaxFloat
 * @author Laurenz Fiala
 * @since 2018/01/21
 */
public class MaxFloatValidator implements ConstraintValidator<MaxFloat, Float> {

	private Float maxValue;

	@Override
	public void initialize(MaxFloat annotation) {
		this.maxValue = annotation.value();
	}

	@Override
	public boolean isValid(Float validationValue, ConstraintValidatorContext validationContext) {
		return validationValue <= this.maxValue;
	}
	
}
