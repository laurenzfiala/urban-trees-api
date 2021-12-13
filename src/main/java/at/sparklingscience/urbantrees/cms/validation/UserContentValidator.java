package at.sparklingscience.urbantrees.cms.validation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PathMatcher;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.cms.UserContentConfiguration;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.SimpleValidationException;

/**
 * Handles validation of user content.
 * @author Laurenz Fiala
 * @since 2021/08/02
 */
public class UserContentValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentValidator.class);

	/**
	 * ANT path matcher used for matching content IDs to configured path
	 * expressions for validation.
	 */
	public PathMatcher pathMatcher;
	
	/**
	 * Stores validation config.
	 * Maps ANT-path-expression as string to a {@link PathConfig}.
	 */
	private Map<String, PathConfig> config = new HashMap<>();
	
	/**
	 * @param pathMatcher ANT path matcher used for matching content
	 * 					  IDs to configured path expressions for
	 * 					  validation.
	 */
	public UserContentValidator(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher; 
	}
	
	/**
	 * Check if the given content matches the requirements for the
	 * given contentPath.
	 * @param contentPath content path to be stored
	 * @param cmsContent content to be stored at the given contentPath
	 */
	public void check(@NotNull String contentPath,
					  @NotNull CmsContent cmsContent) throws SimpleValidationException {

		SimpleErrors errors = new SimpleErrors(contentPath);
		
		boolean anyMatch = false;
		for (Map.Entry<String, PathConfig> configEntry : this.config.entrySet()) {
			
			if(this.pathMatcher.match(configEntry.getKey(), contentPath)) {
				anyMatch = true;
				configEntry.getValue().check(contentPath, cmsContent, errors);
			}
			
		}

		if (!anyMatch) {
			LOGGER.info("Failed content validation: no config for path '{}' was found.", contentPath);
			throw new SimpleValidationException("Given content path may not be saved.", errors);
		} else if (errors.hasErrors()) {
			throw new SimpleValidationException("Invalid content for contentPath: '" + contentPath + "'", errors);
		}
		
	}
	
	/**
	 * Check the given content path before storing the associated user content.
	 * The given path must match the regex defined in
	 * {@link UserContentConfiguration#CONTENT_PATH_VALIDATION_REGEX} or a
	 * {@link BadRequestException} is thrown.
	 * @param contentPath path to check
	 * @throws BadRequestException if the path contains illegal characters or
	 * 							   is malformed
	 */
	public void checkPath(@NotNull String contentPath) throws BadRequestException {
		
		if (!contentPath.matches(UserContentConfiguration.CONTENT_PATH_VALIDATION_REGEX)) {
			throw new BadRequestException("Illegal content path");
		}
		
	}
	
	/**
	 * Check a user-given content path expression for validity and throw a
	 * {@link BadRequestException} if it's invalid.
	 * The given expression must match
	 * {@link UserContentConfiguration#CONTENT_PATH_EXP_VALIDATION_REGEX}.
	 * @param contentPathExp expression given by the user/frontend
	 * @throws BadRequestException if the expression is invalid
	 */
	public void checkPathExp(@NotNull String contentPathExp) throws BadRequestException {
		
		if (!contentPathExp.matches(UserContentConfiguration.CONTENT_PATH_EXP_VALIDATION_REGEX)) {
			throw new BadRequestException("Illegal content path expression");
		}
		
	}
	
	/**
	 * Create a new configuration for the given ANT path-expression
	 * and return a new {@link PathConfig.Builder} for the created PathConfig.
	 * Configure the pathconfig with the return value.
	 * @param antPattern ANT path the path config should apply to
	 * @return path config builder that configures the created path config
	 * 		   instance
	 */
	public PathConfig.Builder path(String antPattern) {
		
		PathConfig pathConfig = new PathConfig();
		this.config.put(antPattern, pathConfig);
		
		return PathConfig.Builder.newInstance(this, pathConfig);
		
	}
	
	/**
	 * Defines configuration for a path expression.
	 */
	public static class PathConfig {
		
		/**
		 * Holds all rules that apply to a specific ANT-style path expression
		 * defined in {@link UserContentPathValidator}.
		 */
		private List<Rule> rules = new LinkedList<>();
		
		/**
		 * Check whether the given cms content adheres to all configured
		 * restrictions. Results are written to errors.
		 * @param contentPath actual content path to be saved
		 * @param cmsContent content to be saved
		 * @param errors used to populate with validation errors
		 */
		public void check(@NotNull String contentPath,
						  @NotNull CmsContent cmsContent,
						  @NotNull SimpleErrors errors) {
			
			this.rules.forEach(r -> {
				r.check(contentPath, cmsContent, errors);
			});
			
		}
		
		/**
		 * Convenience builder that operates on an existing {@link PathConfig}
		 * instance and immediately commits all operations to that instance.
		 */
		public static class Builder {
			
			private UserContentValidator parent;
			private PathConfig pathConfig;
			
			public Builder rule(Rule rule) {
				this.pathConfig.rules.add(rule);
				return this;
			}
			
			public Builder path(String antPattern) {
				return this.parent.path(antPattern);
			}
			
			public static Builder newInstance(UserContentValidator parent, PathConfig pathConfig) {
				Builder builder = new Builder();
				builder.parent = parent;
				builder.pathConfig = pathConfig;
				return builder;
			}
			
		}
		
	}
	
}
