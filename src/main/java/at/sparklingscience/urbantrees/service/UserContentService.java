package at.sparklingscience.urbantrees.service;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.sparklingscience.urbantrees.domain.UserContent;
import at.sparklingscience.urbantrees.domain.UserContentTag;
import at.sparklingscience.urbantrees.exception.UnauthorizedException;
import at.sparklingscience.urbantrees.mapper.UserContentMapper;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import at.sparklingscience.urbantrees.security.authentication.AuthenticationToken;

/**
 * Service for user-content actions.
 * 
 * @author Laurenz Fiala
 * @since 2019/03/13
 */
@Service
public class UserContentService {
	
	private static Logger logger;
	
	@Autowired
    private UserContentMapper contentMapper;
	
	public UserContentService(Logger classLogger) {
		logger = classLogger;
	}
	
	public UserContent getContent(AuthenticationToken authToken, int id) {
		
		logger.debug("get user content with id ", id);
		
		UserContent uc = this.contentMapper.findContent(id, null).get(0);
		
		if (uc.getTag().allowUserAnonymous() ||
			SecurityUtil.hasAllAuthorities(authToken.getAuthorities(), uc.getTag().getAuthoritiesNeeded())) {
			return uc;
		}
		throw new UnauthorizedException("User is not allowed to access user-generated content with tag " + uc.getTag());
		
	}
	
	public List<UserContent> getContent(AuthenticationToken authToken, UserContentTag tag) {
		
		logger.debug("get user content with tag ", tag);
		if (!tag.allowUserAnonymous() &&
			!SecurityUtil.hasAllAuthorities(authToken.getAuthorities(), tag.getAuthoritiesNeeded())) {
			throw new UnauthorizedException("User is not allowed to access user-generated content with tag " + tag);
		}
		return this.contentMapper.findContent(null, tag.name());
		
	}

}
