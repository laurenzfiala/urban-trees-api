package at.sparklingscience.urbantrees.cms;

import java.nio.file.Path;

import at.sparklingscience.urbantrees.cms.component.FileComponent;
import at.sparklingscience.urbantrees.cms.component.TextComponent;
import at.sparklingscience.urbantrees.cms.layout.TwoColumnLayout;
import at.sparklingscience.urbantrees.cms.validation.ElementAllowlistRule;
import at.sparklingscience.urbantrees.cms.validation.UserContentValidator;

/**
 * Holds configuration used for user content.
 * @author Laurenz Fiala
 * @since 2021/08/02
 */
public class UserContentConfiguration {
	
	/**
	 * The root directory relative to the service working directory.
	 * No files may be stored or served outside of this direcoty.
	 */
	public static final Path FILE_ROOT = Path.of("data", "content_file");
	
	/**
	 * String that separates the path segments.
	 * Note: Also update {@link #CONTENT_PATH_VALIDATION_REGEX}.
	 */
	public static final String CONTENT_PATH_SEPARATOR = "/";
	
	/**
	 * Check all content paths against this regex.
	 * If a path does not match, deny the content from being stored.
	 * 
	 * Rules for content paths:
	 * - path segments must be sparated by /
	 * - path segments may only be alphanumeric (upper- and lowercase allowed)
	 * - path segments may not be empty
	 * - path must not end with /
	 * - path must have at least one path segment
	 */
	public static final String CONTENT_PATH_VALIDATION_REGEX = "(\\/[a-zA-Z0-9]+)+";
	
	/**
	 * Check content path expressions against this regex.
	 * If a path does not match, don't issue the query to the DB.
	 * 
	 * Rules for content path queries:
	 * - path segments must be sparated by /
	 * - path segments may be alphanumeric, * or **
	 * - path segments may not be empty
	 * - path must not end with /
	 * - path must have at least one path segment
	 */
	public static final String CONTENT_PATH_EXP_VALIDATION_REGEX = "(\\/(([a-zA-Z0-9]+)|\\*{1,2}))+";
	
	public UserContentConfiguration(UserContentValidator validator) {
		this.configure(validator);
	}
	
	/**
	 * Configure user content/CMS validation.
	 * @param validator validator to configure here
	 */
	private void configure(UserContentValidator validator) {
		
		validator
			.path("/test/*")
				.rule(new ElementAllowlistRule(TextComponent.class, TwoColumnLayout.class, FileComponent.class));
		
	}
	
}
