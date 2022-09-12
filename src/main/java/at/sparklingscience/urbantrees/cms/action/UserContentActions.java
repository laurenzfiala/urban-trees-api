package at.sparklingscience.urbantrees.cms.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PathMatcher;

import at.sparklingscience.urbantrees.cms.UserContentConfiguration;
import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Handles post-save user content actions.
 * @author Laurenz Fiala
 * @since 2022/03/22
 */
public class UserContentActions {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentActions.class);

	/**
	 * ANT path matcher used for matching content IDs to configured path
	 * expressions for validation.
	 */
	public PathMatcher pathMatcher;
	
	/**
	 * Stores action config.
	 * Maps ANT-path-expression as string to a {@link PathConfig}.
	 */
	private Map<String, PathConfig> config = new HashMap<>();
	
	/**
	 * @param pathMatcher ANT path matcher used for matching content
	 * 					  IDs to configured path expressions for
	 * 					  validation.
	 */
	public UserContentActions(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher; 
	}
	
	/**
	 * Execute all actions registered for the given contentPath.
	 * @param contentPath content path to be stored
	 * @param userContent saved user content
	 * @param authToken users' auth token
	 */
	public void doActions(@NotNull String contentPath,
						  @NotNull UserContent userContent,
						  @NotNull AuthenticationToken authToken) {
		
		try {			
			for (Map.Entry<String, PathConfig> configEntry : this.config.entrySet()) {
				
				if(this.pathMatcher.match(configEntry.getKey(), contentPath)) {
					configEntry.getValue().doAction(userContent, authToken);
				}
				
			}
		} catch (RuntimeException e) {
			LOGGER.error("User content action failed: " + e.getMessage(), e);
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
		 * Holds all actions that apply to a specific ANT-style path expression
		 * defined in {@link UserContentPathValidator}.
		 */
		private List<Action> actions = new LinkedList<>();
		
		/**
		 * Execute all actions.
		 * @param userContent saved user content
		 * @param authToken users' auth token
		 */
		public void doAction(@NotNull UserContent userContent,
						     @NotNull AuthenticationToken authToken) {
			
			this.actions.forEach(a -> {
				a.doAction(userContent, authToken);
			});
			
		}
		
		/**
		 * Convenience builder that operates on an existing {@link PathConfig}
		 * instance and immediately commits all operations to that instance.
		 */
		public static class Builder {
			
			private UserContentActions parent;
			private PathConfig pathConfig;
			
			public Builder action(Action action) {
				this.pathConfig.actions.add(action);
				return this;
			}
			
			public Builder path(String antPattern) {
				return this.parent.path(antPattern);
			}
			
			public static Builder newInstance(UserContentActions parent, PathConfig pathConfig) {
				Builder builder = new Builder();
				builder.parent = parent;
				builder.pathConfig = pathConfig;
				return builder;
			}
			
		}
		
	}
	
}
