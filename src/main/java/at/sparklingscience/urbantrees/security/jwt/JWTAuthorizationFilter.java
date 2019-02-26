package at.sparklingscience.urbantrees.security.jwt;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.AuthSettings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

/**
 * JWT authorization filter to authorize an
 * already logged-in user.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/10
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthorizationFilter.class);
	
	private AuthMapper authMapper;

	public JWTAuthorizationFilter(AuthenticationManager authManager, AuthMapper authMapper) {
        super(authManager);
        this.authMapper = authMapper;
    }
	
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
    	
    	LOGGER.trace("Starting token authentication.");
    	
        final String header = req.getHeader(SecurityConfiguration.HEADER_KEY);

        if (header == null || !header.startsWith(SecurityConfiguration.JWT_TOKEN_PREFIX)) {
        	LOGGER.trace("Skipping token authentication, since header is {}.", header);
            chain.doFilter(req, res);
            return;
        }

        Authentication authentication = this.getAuthentication(header, req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
        
    }

    /**
     * TODO
     * @param token
     * @param request
     * @return
     */
    private Authentication getAuthentication(final String token, HttpServletRequest request) {
    	
        if (token == null) {
        	return null;
        }
        	
    	Claims jwtClaims;
    	try {
    		jwtClaims = Jwts.parser()
    				.setSigningKey(this.authMapper.findSetting(AuthSettings.JWT_SECRET).getBytes())
    				.parseClaimsJws(token.replace(SecurityConfiguration.JWT_TOKEN_PREFIX, ""))
    				.getBody();
    	} catch (SignatureException e) {
    		LOGGER.warn("Users' auth token is untrusted {}", e.getMessage(), e);
    		return null;
    	} catch (ExpiredJwtException e) {
    		LOGGER.trace("Users' auth token has expired: {}", e.getMessage(), e);
    		return null;
    	}
    	
    	final int userId = jwtClaims.get(SecurityConfiguration.JWT_CLAIMS_USERID_KEY, Integer.class);
        final String username = jwtClaims.getSubject();
        final List<GrantedAuthority> roles = AuthorityUtils.commaSeparatedStringToAuthorityList(jwtClaims.get(SecurityConfiguration.JWT_CLAIMS_ROLES_KEY, String.class));
        final Date tokenExpirationDate = jwtClaims.getExpiration();
        final Date tokenCreationDate = new Date(tokenExpirationDate.getTime() - SecurityConfiguration.JWT_EXPIRATION_TIME);
        
        if (username != null) {
        	LOGGER.trace("Setting username/password auth token for user {}.", username);
            return new AuthenticationToken(userId, username, roles, tokenCreationDate);
        }
        return null;
        
    }
	
}
