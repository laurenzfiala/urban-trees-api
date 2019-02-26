package at.sparklingscience.urbantrees.security.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.domain.User;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.AuthSettings;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import io.jsonwebtoken.Jwts;

/**
 * Authentication filter used to
 * authenticate not logged-in users.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/10
 */
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
	
	/**
	 * Spring integrated auth manager.
	 */
	private AuthenticationManager authenticationManager;
	
	private AuthMapper authMapper;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, AuthMapper authMapper) {
		this.authenticationManager = authenticationManager;
		this.authMapper = authMapper;
	}

	@Override
	protected void initFilterBean() throws ServletException {
		super.initFilterBean();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        this.authMapper = webApplicationContext.getBean(AuthMapper.class);
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {

		User creds = null;
		try {
			creds = new ObjectMapper().readValue(req.getInputStream(), User.class);
			LOGGER.trace("Got user info for user {}", creds.getUsername());

			this.authMapper.increaseFailedLoginAttemptsByUsername(creds.getUsername());

			Authentication auth;
			if (creds.getSecureLoginKey() == null) {
				auth = new UsernamePasswordAuthenticationToken(
						creds.getUsername(),
						creds.getPassword()
						);
			} else {
				auth = new TokenAuthenticationToken(creds.getSecureLoginKey());
			}
			
			return authenticationManager.authenticate(auth);
							
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (creds != null && creds.getUsername() != null) {
				this.authMapper.updateLastLoginAttemptDatByUsername(creds.getUsername());
			}
		}

	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, ServletException {

		at.sparklingscience.urbantrees.security.user.User user =
				(at.sparklingscience.urbantrees.security.user.User) auth.getDetails();
		
		Collection<? extends GrantedAuthority> authorities = null;
		if (!user.isCredentialsNonExpired()) {
			// TODO log
			authorities = Arrays.asList(SecurityUtil.grantedAuthority(SecurityConfiguration.TEMPORARY_CHANGE_PASSWORD_ACCESS_ROLE));
		}
		
		LOGGER.trace("Successful authentication, creating token for user {}.", auth.getPrincipal());
		
		this.authMapper.resetFailedLoginAttempts(user.getId());
		this.authMapper.updateLastLoginDat(user.getId());
		this.authMapper.updateUserLoginKey(user.getId(), null);
		
		final String token = Jwts.builder().setSubject(user.getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConfiguration.JWT_EXPIRATION_TIME))
				.addClaims(this.getUserClaims(user, authorities))
				.signWith(SecurityConfiguration.JWT_AUTHENTICATION_SIG_ALG, this.getJWTSecret())
				.compact();
		
		res.addHeader("Access-Control-Expose-Headers", SecurityConfiguration.HEADER_KEY);
		res.addHeader(SecurityConfiguration.HEADER_KEY, SecurityConfiguration.JWT_TOKEN_PREFIX + token);

	}
	
	/**
	 * Fetches the users' roles and returns them to be added
	 * to the JWT token as additional claims.
	 * @param user User to get information from
	 * @param overrideAuthorities null if users' authorities should be used; otherwise use this parameter
	 */
	private Map<String, Object> getUserClaims(at.sparklingscience.urbantrees.security.user.User user,
											  Collection<? extends GrantedAuthority> overrideAuthorities) {
		
		if (overrideAuthorities == null) {
			overrideAuthorities = user.getAuthorities();
		}
		
		Map<String, Object> userClaims = new HashMap<>(2);
		userClaims.put(
				SecurityConfiguration.JWT_CLAIMS_USERID_KEY,
				user.getId()
				);
		userClaims.put(
				SecurityConfiguration.JWT_CLAIMS_ROLES_KEY,
				overrideAuthorities.stream().map(ga -> ga.getAuthority()).collect(Collectors.joining(","))
				);
		
		return userClaims;
		
	}
	
	/**
	 * Fetches the JWT secret from the database, used for
	 * signing the JWT tokens.
	 * DB-call is cached by mybatis.
	 */
	private byte[] getJWTSecret() {
		
		return this.authMapper.findSetting(AuthSettings.JWT_SECRET).getBytes();
		
	}

}
