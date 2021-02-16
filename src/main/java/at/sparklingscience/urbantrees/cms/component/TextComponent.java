package at.sparklingscience.urbantrees.cms.component;

import org.springframework.validation.Errors;

import at.sparklingscience.urbantrees.cms.CmsElement;

public class TextComponent implements CmsElement {
	
	private String text;

	@Override
	public void validate(Errors errors) {
		
		if (this.text == null || this.text.isBlank()) {
			errors.rejectValue("text", "Text may not be empty");
		}
		
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
}
