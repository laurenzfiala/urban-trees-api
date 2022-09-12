package at.sparklingscience.urbantrees.cms.layout;

import java.util.List;

import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import at.sparklingscience.urbantrees.cms.CmsElement;
import at.sparklingscience.urbantrees.util.ListUtils;

public class BlockLayout implements CmsElement {

	@JsonProperty
	private CmsElement slot;

	@Override
	public void validate(Errors errors) {
		
		if (this.slot == null) {
			errors.rejectValue("slot", "Slot may not be null");
		}
		errors.pushNestedPath("slot");
		this.slot.validate(errors);
		errors.popNestedPath();
		
	}
	
	@Override
	public void sanitize() {}

	@Override
	public List<CmsElement> getChildren() {
		return ListUtils.ofNonNull(this.slot);
	}

	public CmsElement getSlot() {
		return slot;
	}

	public void setSlot(CmsElement slot) {
		this.slot = slot;
	}

}
