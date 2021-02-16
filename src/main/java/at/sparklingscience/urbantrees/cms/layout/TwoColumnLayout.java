package at.sparklingscience.urbantrees.cms.layout;

import org.springframework.validation.Errors;

import at.sparklingscience.urbantrees.cms.CmsElement;

public class TwoColumnLayout implements CmsElement {

	private CmsElement slotLeft;
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
