package at.sparklingscience.urbantrees.cms;

import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME, 
	include = JsonTypeInfo.As.PROPERTY,
	property = "name"
)
@JsonTypeIdResolver(CmsElementResolver.class)
public interface CmsElement {
	
	/**
	 * Validates the CMS element.
	 * @param errors controller's errors
	 */
	void validate(Errors errors);
	
}
