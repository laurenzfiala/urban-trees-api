package at.sparklingscience.urbantrees.cms;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import at.sparklingscience.urbantrees.cms.action.UserContentActions;
import at.sparklingscience.urbantrees.cms.action.XpRewardAction;
import at.sparklingscience.urbantrees.cms.component.FileComponent;
import at.sparklingscience.urbantrees.cms.component.ImageComponent;
import at.sparklingscience.urbantrees.cms.component.TextComponent;
import at.sparklingscience.urbantrees.cms.layout.BlockLayout;
import at.sparklingscience.urbantrees.cms.validation.ElementAllowlistRule;
import at.sparklingscience.urbantrees.cms.validation.IdentityRule;
import at.sparklingscience.urbantrees.cms.validation.UserContentValidator;
import at.sparklingscience.urbantrees.domain.UserLevelAction;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;
import at.sparklingscience.urbantrees.service.UserService;

/**
 * Holds configuration used for user content.
 * @author Laurenz Fiala
 * @since 2021/08/02
 */
@Configuration
public class UserContentConfiguration {
	
	@Autowired
	private ApplicationContext applicationContext;
	
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
	
	
	@Bean
	public PathMatcher userContentPathMatcher() {
		return new AntPathMatcher(CONTENT_PATH_SEPARATOR);
	}
	
	@Bean
	public UserContentValidator userContentValidator() {
		UserContentValidator validator = new UserContentValidator(this.userContentPathMatcher());
		validator
			.path("/tree/{treeId:\\d+}")
				.rule(new ElementAllowlistRule(TextComponent.class, FileComponent.class, ImageComponent.class, BlockLayout.class))
			.path("/methodbox")
				.rule(new ElementAllowlistRule(TextComponent.class, FileComponent.class, ImageComponent.class, BlockLayout.class))
			.path("/user/{userId:\\d+}/**")
				.rule(new IdentityRule((Map<String, String> uriVars, AuthenticationToken auth) -> Integer.parseInt(uriVars.get("userId")) == auth.getId()));
		
		return validator;
	}
	
	@Bean
	public UserContentActions userContentActions() {
		UserContentActions actions = new UserContentActions(this.userContentPathMatcher());
		actions
			.path("/tree/*")
				.action(new XpRewardAction(this.applicationContext.getBean(UserService.class), UserLevelAction.USER_TREE_CONTENT_SUBMIT))
			.path("/user/*/expdays")
				.action(new XpRewardAction(this.applicationContext.getBean(UserService.class), UserLevelAction.USER_EXP_DAYS_SUBMIT));
		
		return actions;
	}
	
}
