package at.sparklingscience.urbantrees.domain.validator;

import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange.Range;

/**
 * Validates the given {@link Date} for its presence in a given {@link DateRange}.
 * 
 * Since 2020/12/29: Null values are allowed.
 * 
 * @see DateRange
 * @author Laurenz Fiala
 * @since 2018/01/21
 */
public class DateRangeValidator implements ConstraintValidator<DateRange, Date> {

	/**
	 * Date range given.
	 * @see Range
	 */
	private Range range;
	
	@Override
	public void initialize(DateRange annotation) {
		this.range = annotation.value();
	}

	@Override
	public boolean isValid(Date validationDate, ConstraintValidatorContext validationContext) {
		
		if (validationDate == null) {
			return true;
		}

		int compareResult = validationDate.compareTo(new Date());
		switch (this.range) {
		
			case ANY:
				return true;
				
			case PAST:
				return compareResult < 0;
				
			case PAST_AND_PRESENT:
				return compareResult <= 0;
				
			case PRESENT:
				return compareResult == 0;
				
			case FUTURE:
				return compareResult > 0;
				
			case FUTURE_AND_PRESENT:
				return compareResult >= 0;
				
		}
		
		return false;

	}
	
}
