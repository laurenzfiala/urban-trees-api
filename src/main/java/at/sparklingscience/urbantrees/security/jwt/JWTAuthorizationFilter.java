package at.sparklingscience.urbantrees.security.jwt;

import java.io.IOException;
import java.security.Key;
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
import at.sparklingscience.urbantrees.security.user.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

/**
 * JWT authorization filter to authorize an
 * already logged-in user.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/10
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
	
	private static Logger logger = LoggerFactory.getLogger(JWTAuthorizationFilter.class);
	
	private AuthenticationService authService;

	public JWTAuthorizationFilter(AuthenticationManager authManager, AuthenticationService authService) {
        super(authManager);
        this.authService = authService;
    }
	
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
    	
    	logger.trace("Starting token authentication.");
    	
        final String header = req.getHeader(SecurityConfiguration.HEADER_KEY);

        if (header == null || !header.startsWith(SecurityConfiguration.JWT_TOKEN_PREFIX)) {
        	logger.trace("Skipping token authentication, since header is {}.", header);
            chain.doFilter(req, res);
            return;
        }

        Authentication authentication = this.getAuthentication(header, req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
        
    }

    private Authentication getAuthentication(final String token, HttpServletRequest request) {
    	
        if (token == null) {
        	return null;
        }
        	
    	Claims jwtClaims;
    	try {
    		SigningKeyResolver signingKeyResolver = new SigningKeyResolverAdapter() {
    			@Override
				public Key resolveSigningKey(@SuppressWarnings("rawtypes") JwsHeader header, Claims claims) {
    				final int userId = claims.get(SecurityConfiguration.JWT_CLAIMS_USERID_KEY, Integer.class);
    				return authService.getJWTSecret(userId);
				}
			};
    		
    		jwtClaims = Jwts.parserBuilder()
    				.setSigningKeyResolver(signingKeyResolver)
    				.build()
    				.parseClaimsJws(token.replace(SecurityConfiguration.JWT_TOKEN_PREFIX, ""))
    				.getBody();
    	} catch (SignatureException e) {
    		logger.warn("Users' auth token is untrusted {}", e.getMessage(), e);
    		return null;
    	} catch (ExpiredJwtException e) {
    		logger.trace("Users' auth token has expired: {}", e.getMessage(), e);
    		return null;
    	} catch (UnsupportedJwtException e) {
    		logger.trace("Users' auth token is unsupported: {}", e.getMessage(), e);
    		return null;
    	} catch (MalformedJwtException e) {
    		logger.trace("Users' auth token is malformed: {}", e.getMessage(), e);
    		return null;
    	}
    	
    	final int userId = jwtClaims.get(SecurityConfiguration.JWT_CLAIMS_USERID_KEY, Integer.class);
        final String username = jwtClaims.getSubject();
        final List<GrantedAuthority> roles = AuthorityUtils.commaSeparatedStringToAuthorityList(jwtClaims.get(SecurityConfiguration.JWT_CLAIMS_ROLES_KEY, String.class));
        final Date tokenExpirationDate = jwtClaims.getExpiration();
        final Date tokenCreationDate = new Date(tokenExpirationDate.getTime() - SecurityConfiguration.JWT_EXPIRATION_TIME);
        
        if (username != null) {
        	logger.trace("Setting username/password auth token for user {}.", username);
            return new AuthenticationToken(userId, username, roles, tokenCreationDate);
        }
        return null;
        
    }
	
}
