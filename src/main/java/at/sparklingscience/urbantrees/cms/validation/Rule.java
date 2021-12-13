package at.sparklingscience.urbantrees.cms.validation;

import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.cms.CmsContent;

/**
 * A user content rule enforcing some restriction on
 * the given {@link CmsContent}.
 * @author Laurenz Fiala
 * @since 2021/08/02
 */
public interface Rule {
	
	/**
	 * Check the given content path/content combination and add
	 * validation errors to errors-parameter.
	 * @param contentPath content path to validate
	 * @param cmsContent content to validate
	 * @param errors when complete, holds all validation errors
	 */
	void check(@NotNull String contentPath,
	  		   @NotNull CmsContent cmsContent,
	  		   @NotNull SimpleErrors errors);
	
}
