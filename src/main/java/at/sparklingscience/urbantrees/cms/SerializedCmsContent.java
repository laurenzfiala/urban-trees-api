package at.sparklingscience.urbantrees.cms;

import java.util.List;

import org.springframework.validation.Errors;

/**
 * Holds a single CMS content entry (without metadata).
 * This is received from the frontend as part of {@link CmsContent}.
 * 
 * @author Laurenz Fiala
 * @since 2020/01/25
 */
public class SerializedCmsContent {
	
	/**
	 * Version of the generated content.
	 */
	private int version;

	/**
	 * The types of {@link CmsElement}s inside are decided depending on their JSON "name"-field.
	 * The deserialized objects are the used to validate them.
	 */
	private List<CmsElement> elements;
	
	/**
	 * Validate all {@link #elements}.
	 * @param errors errors object from the controller.
	 */
	public void validate(Errors errors) {
		
		for (int i = 0; i < this.elements.size(); i++) {
			errors.pushNestedPath("elements[" + i + "]");
			this.elements.get(i).validate(errors);
			errors.popNestedPath();
		}
		
	}

	public List<CmsElement> getElements() {
		return elements;
	}

	public void setElements(List<CmsElement> elements) {
		this.elements = elements;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
}
