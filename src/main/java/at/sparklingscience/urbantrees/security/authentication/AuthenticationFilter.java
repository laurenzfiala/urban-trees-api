package at.sparklingscience.urbantrees.security.authentication;

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
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.security.SecurityUtil;
import at.sparklingscience.urbantrees.security.authentication.jwt.JWTUserAuthentication;
import at.sparklingscience.urbantrees.security.authentication.otk.TokenAuthenticationToken;
import at.sparklingscience.urbantrees.security.authentication.otp.IncorrectOtpTokenException;
import at.sparklingscience.urbantrees.security.authentication.otp.UserOtpAuthenticationToken;
import at.sparklingscience.urbantrees.security.authentication.user.UserAuthenticationToken;
import at.sparklingscience.urbantrees.service.AuthenticationService;
import io.jsonwebtoken.Jwts;

/**
 * Authentication filter used to map the incoming
 * login request to the various means of authentication.
 * 
 * These are:
 * - username and password
 * - username and password and OTP
 * - a secure login key
 * 
 * After successful authentication handled by the various {@link AuthenticationProvider}s,
 * {@link #successfulAuthentication(HttpServletRequest, HttpServletResponse, FilterChain, Authentication)}
 * constructs a new JWT token, perists the session, and hands it back to the client.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/10
 */
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter implements AuthenticationFailureHandler {
	
	/**
	 * HTTP header field to send back in case of incomplete authentication
	 * request.
	 */
	private static final String INCOMPLETE_HEADER_KEY = "Authentication-Requires";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
	
	/**
	 * Object mapper used to deserialize JWT tokens.
	 * 
	 * Note: dont use this directly, but call {@link ObjectMapper#reader()} or {@link ObjectMapper#writer()}
	 * 		 to get a immutable representation.
	 */
	//private static ObjectMapper objectMapper = new ObjectMapper(); TODO remove
	private ObjectMapper jsonObjectMapper;
	
	/**
	 * Spring integrated auth manager.
	 */
	private AuthenticationManager authenticationManager;
	
	private AuthenticationService authService;
	
	public AuthenticationFilter(AuthenticationManager authenticationManager,
								AuthenticationService authService,
								ObjectMapper jsonObjectMapper) {
		this.authenticationManager = authenticationManager;
		this.authService = authService;
		this.jsonObjectMapper = jsonObjectMapper;
	}

	@Override
	protected void initFilterBean() throws ServletException {
		super.initFilterBean();
		
		//WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        //this.authService = webApplicationContext.getBean(AuthenticationService.class);
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {

		UserCredentials creds = null;
		try {
			
			creds = this.jsonObjectMapper.reader().readValue(req.getInputStream(), UserCredentials.class);
			LOGGER.trace("Got user info for user {}", creds.getUsername());

			Authentication auth;
			if (creds.getSecureLoginKey() != null) {
				auth = new TokenAuthenticationToken(creds.getSecureLoginKey());
			} else if (creds.getOtp() != null) {
				auth = new UserOtpAuthenticationToken(
						creds.getUsername(),
						creds.getPassword(),
						creds.getOtp()
						);
			} else {
				auth = new UserAuthenticationToken(
						creds.getUsername(),
						creds.getPassword()
						);
			}
			
			return authenticationManager.authenticate(auth);
							
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (IncorrectTokenException e) {
			LOGGER.trace("Incorrect token given");
			throw e;
		} catch (Throwable e) {
			if (creds != null && creds.getUsername() != null) {
				this.authService.increaseFailedLoginAttempts(creds.getUsername());				
			}
			throw e;
		} finally {
			if (creds != null && creds.getUsername() != null) {
				this.authService.updateLastLoginAttemptDat(creds.getUsername());
			}
		}

	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, ServletException {

		final AuthenticationToken authToken = ControllerUtil.getAuthToken(auth);
		at.sparklingscience.urbantrees.security.user.User user =
				(at.sparklingscience.urbantrees.security.user.User) auth.getDetails();
		
		Collection<? extends GrantedAuthority> authorities = null;
		if (!user.isCredentialsNonExpired()) {
			LOGGER.info("Credentials of user {} are expired. Granting temp change role only.", user.getId());
			authorities = Arrays.asList(SecurityUtil.grantedAuthority(SecurityConfiguration.TEMPORARY_CHANGE_PASSWORD_ACCESS_ROLE));
		} else if (!this.getEnvironment().acceptsProfiles(Profiles.of("dev")) &&
				SecurityUtil.isAdmin(authToken) &&
				!user.isUsingOtp()) {
			LOGGER.info("User {} is admin and OTP is deactivated. Granting temp OTP activation role only.", user.getId());
			authorities = Arrays.asList(SecurityUtil.grantedAuthority(SecurityConfiguration.TEMPORARY_ACTIVATE_OTP_ACCESS_ROLE));
		}
		
		LOGGER.trace("Successful authentication, creating token for user {}.", user.getId());
		
		this.authService.successfulAuth(user.getId());
		
		final JWTUserAuthentication session = this.authService.newSession(user);
		final String token = Jwts.builder().setSubject(user.getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConfiguration.JWT_EXPIRATION_TIME))
				.addClaims(this.getUserClaims(user, authorities, session))
				.signWith(session.getSecret())
				.compact();
		
		res.addHeader("Access-Control-Expose-Headers", SecurityConfiguration.HEADER_KEY);
		res.addHeader(SecurityConfiguration.HEADER_KEY, SecurityConfiguration.JWT_TOKEN_PREFIX + token);

	}
	
	/**
	 * Converts the users' roles, user id and auth id to a map for inclusion
	 * the the JWT claims.
	 * @param user User to get information from
	 * @param overrideAuthorities null if users' authorities should be used; otherwise use this parameter
	 * @param session users' new session
	 */
	private Map<String, Object> getUserClaims(at.sparklingscience.urbantrees.security.user.User user,
											  Collection<? extends GrantedAuthority> overrideAuthorities,
											  JWTUserAuthentication session) {
		
		if (overrideAuthorities == null) {
			overrideAuthorities = user.getAuthorities();
		}
		
		Map<String, Object> userClaims = new HashMap<>(2);
		userClaims.put(
				SecurityConfiguration.JWT_CLAIMS_USERID_KEY,
				user.getId()
				);
		userClaims.put(
				SecurityConfiguration.JWT_CLAIMS_AUTHID_KEY,
				session.getId()
				);
		userClaims.put(
				SecurityConfiguration.JWT_CLAIMS_ROLES_KEY,
				overrideAuthorities.stream().map(ga -> ga.getAuthority()).collect(Collectors.joining(","))
				);
		
		return userClaims;
		
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		LOGGER.trace("Incorrect token given, responding with appropriate header.");
		if (exception instanceof IncorrectOtpTokenException) {
			response.addHeader("Access-Control-Expose-Headers", INCOMPLETE_HEADER_KEY);
			response.addHeader(INCOMPLETE_HEADER_KEY, ((IncorrectOtpTokenException) exception).flag());
		}
		response.setStatus(HttpStatus.FORBIDDEN.value());
		
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		this.setAuthenticationFailureHandler(this);
		super.unsuccessfulAuthentication(request, response, failed);
	}

}
