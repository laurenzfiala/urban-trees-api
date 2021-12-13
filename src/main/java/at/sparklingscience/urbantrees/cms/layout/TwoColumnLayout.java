package at.sparklingscience.urbantrees.cms.layout;

import java.util.LinkedList;
import java.util.List;

import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import at.sparklingscience.urbantrees.cms.CmsElement;

public class TwoColumnLayout implements CmsElement {

	@JsonProperty
	private CmsElement slotLeft;
	
	@JsonProperty
	private CmsElement slotRight;

	@Override
	public void validate(Errors errors) {
		
		if (this.slotLeft == null) {
			errors.rejectValue("slotLeft", "Left column may not be empty");
		} else {
			errors.pushNestedPath("slotLeft");
			this.slotLeft.validate(errors);
			errors.popNestedPath();
		}
		if (this.slotRight == null) {
			errors.rejectValue("slotRight", "Right column may not be empty");
		} else {
			errors.pushNestedPath("slotRight");
			this.slotRight.validate(errors);
			errors.popNestedPath();
		}
		
	}
	
	@Override
	public void sanitize() {

		this.slotLeft.sanitize();
		this.slotRight.sanitize();
		
	}

	@Override
	public List<CmsElement> getChildren() {
		
		List<CmsElement> elements = new LinkedList<>();
		elements.add(this.slotLeft);
		elements.addAll(this.slotLeft.getChildren());
		elements.add(this.slotRight);
		elements.addAll(this.slotRight.getChildren());
		
		return elements;
		
	}

	public CmsElement getSlotLeft() {
		return slotLeft;
	}

	public void setSlotLeft(CmsElement slotLeft) {
		this.slotLeft = slotLeft;
	}

	public CmsElement getSlotRight() {
		return slotRight;
	}

	public void setSlotRight(CmsElement slotRight) {
		this.slotRight = slotRight;
	}
	
}
