package at.sparklingscience.urbantrees.cms.validation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.cms.CmsElement;
import at.sparklingscience.urbantrees.exception.ValidationException;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Create a new allowlist rule where only the passed element types may
 * be present in the {@link CmsContent}. Order or amount of times the
 * elements occur are not checked.
 * @author Laurenz Fiala
 * @since 2022/09/27 (doc)
 */
public class ElementAllowlistRule implements Rule {
	
	private List<Class<? extends CmsElement>> allowList = new LinkedList<>();
	
	/**
	 * Create a new allow list rule.
	 * @param allowElements element classes to allow
	 * @see ElementAllowlistRule
	 */
	@SafeVarargs
	public ElementAllowlistRule(Class<? extends CmsElement> ...allowElements) {
		
		this.allowList.addAll(Arrays.asList(allowElements));
		
	}
	
	@Override
	public void check(@NotNull String contentId,
			   		  @NotNull Map<String, String> uriVars,
					  @NotNull AuthenticationToken authToken,
					  @NotNull CmsContent cmsContent,
					  @NotNull SimpleErrors errors) throws ValidationException {
		
		this.checkElementsRecursive(cmsContent.getContent().getElements(), errors);
		
	}
	
	private void checkElementsRecursive(List<CmsElement> children, SimpleErrors errors) {
		
		children.forEach(e -> {
			
			Class<? extends CmsElement> componentClass = e.getClass();
			errors.pushNestedPath(componentClass.getSimpleName());
			if (!this.allowList.contains(componentClass)) {
				errors.reject("This CMS component is not allowed for this content ID.");
			} else {
				this.checkElementsRecursive(e.getChildren(), errors);
			}
			errors.popNestedPath();
			
		});
		
	}
	
}
