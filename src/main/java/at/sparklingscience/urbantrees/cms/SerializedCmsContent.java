package at.sparklingscience.urbantrees.cms;

import java.util.LinkedList;
import java.util.List;

import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds a single CMS content entry (without metadata).
 * This is received from the frontend as part of {@link CmsContent}.
 * 
 * Note: the serializer is not allowed to auto-detect anything. json properties
 * 		 have to be declared manually using {@link JsonProperty}.
 * 
 * @author Laurenz Fiala
 * @since 2020/01/25
 */
public class SerializedCmsContent {
	
	/**
	 * Version of the generated content.
	 */
	@JsonProperty
	private int version;

	/**
	 * The types of {@link CmsElement}s inside are decided depending on their JSON "name"-field.
	 * The deserialized objects are the used to validate them.
	 */
	@JsonProperty
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
	
	/**
	 * Sanitize all {@link #elements}.
	 * Needs to be called after {@link #validate(Errors)}.
	 */
	public void sanitize() {
		
		for (CmsElement e : this.elements) {
			e.sanitize();
		}
		
	}

	/**
	 * Return all elements, including indirect children, in a flat list.
	 * @return list of all elements, flattened
	 */
	public List<CmsElement> gatherAllElements() {
		
		List<CmsElement> elements = new LinkedList<>();
		this.elements.forEach(e -> {
			gatherElementsRecursive(elements, e);
		});
		
		return elements;
		
	}
	
	/**
	 * Recursive method going through all children of given element.
	 * @param collection to add found elements to
	 * @param element from which to start (element is also added to collection)
	 */
	private void gatherElementsRecursive(List<CmsElement> collection, CmsElement element) {
		
		collection.add(element);
		element.getChildren().forEach(e -> {
			gatherElementsRecursive(collection, e);
		});
		
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
