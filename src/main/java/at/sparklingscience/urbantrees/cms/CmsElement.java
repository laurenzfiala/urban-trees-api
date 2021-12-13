package at.sparklingscience.urbantrees.cms;

import java.util.List;

import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * Any CMS element (components or layouts) implement this interface.
 * 
 * Note: the serializer is not allowed to auto-detect anything. json properties
 * 		 have to be declared manually using {@link JsonProperty}.
 * 
 * @author Laurenz Fiala
 * @since 2021/08/13 (doc)
 */
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME, 
	include = JsonTypeInfo.As.PROPERTY,
	property = "name"
)
@JsonTypeIdResolver(CmsElementResolver.class)
public interface CmsElement {
	
	/**
	 * Return the child elements of this element.
	 * The default implementation returns an empty unmodifiable list.
	 * @return list of direct child elements
	 */
	default List<CmsElement> getChildren() {
		return List.of();
	};
	
	/**
	 * Validates the CMS element.
	 * @param errors controller's errors
	 */
	void validate(Errors errors);
	
	/**
	 * Implementors sanitize the CMS element.
	 * The default implementation does nothing.
	 * 
	 * After this method is completed, the element is assumed
	 * to be ready for storage.
	 * This method may only be called after {@link #validate(Errors)}
	 * has successfully run on the same unchanged instance.
	 * 
	 * Note: Only allow as little HTML as necessary.
	 */
	default void sanitize() {};
	
}
