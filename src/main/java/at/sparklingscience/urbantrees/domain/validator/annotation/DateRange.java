package at.sparklingscience.urbantrees.domain.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import at.sparklingscience.urbantrees.domain.validator.DateRangeValidator;

/**
 * Validation annotation to validate dates and apply a valid range to them.
 * 
 * @author Laurenz Fiala
 * @since 2018/01/21
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { DateRangeValidator.class })
public @interface DateRange {

	/**
	 * A date range to validate against.
	 */
	public enum Range {

		/**
		 * The date can have any value.
		 */
		ANY,

		/**
		 * Only dates before now are valid.
		 */
		PAST,

		/**
		 * Dates before now and now are valid.
		 */
		PAST_AND_PRESENT,

		/**
		 * Only dates which are exactly now (1 ms) are valid.
		 */
		PRESENT,

		/**
		 * Only dates after now are valid.
		 */
		FUTURE,

		/**
		 * Dates now and after now are valid.
		 */
		FUTURE_AND_PRESENT;

	}

	String message() default "Date is out of range";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * The date range to use for validation.
	 * 
	 * @see Range
	 */
	Range value() default Range.ANY;

}
