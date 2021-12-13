package at.sparklingscience.urbantrees.cms.component;

import org.owasp.html.Sanitizers;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import at.sparklingscience.urbantrees.cms.CmsElement;

public class TextComponent implements CmsElement {
	
	@JsonProperty
	private String text;

	@Override
	public void validate(Errors errors) {
		
		if (this.text == null || this.text.isBlank()) {
			errors.rejectValue("text", "Text may not be empty");
		}
		
	}
	
	@Override
	public void sanitize() {
		
		this.text = Sanitizers.FORMATTING.sanitize(this.text);
		
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
}
