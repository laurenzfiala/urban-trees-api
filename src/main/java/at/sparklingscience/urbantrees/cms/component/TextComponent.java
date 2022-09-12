package at.sparklingscience.urbantrees.cms.component;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import at.sparklingscience.urbantrees.cms.CmsElement;

public class TextComponent implements CmsElement {
	
	/**
	 * Configuration on what the HTML sanitizer allows in the {@link #text} field.
	 */
	private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder()
		.allowElements("h1", "h2", "p", "br", "strong", "em", "ut-cms-text-link")
		.allowAttributes("href", "text").onElements("ut-cms-text-link")
		.toFactory();
	
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
		
		this.text = SANITIZER_POLICY.sanitize(this.text);
		
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
}
