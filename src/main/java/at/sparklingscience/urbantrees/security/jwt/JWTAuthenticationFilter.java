package at.sparklingscience.urbantrees.security.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.SecurityConfiguration;
import at.sparklingscience.urbantrees.mapper.AuthMapper;
import at.sparklingscience.urbantrees.security.AuthSettings;
import at.sparklingscience.urbantrees.security.user.User;
import io.jsonwebtoken.Jwts;

/**
 * Authentication filter used for logged-in users.
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

		try {
			
			User creds = new ObjectMapper().readValue(req.getInputStream(), User.class);
			LOGGER.trace("Got user info for user {}", creds.getUsername());
			
			return authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword(), new ArrayList<>()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, ServletException {

		LOGGER.trace("Successful authentication, creating token for user {}.", auth.getPrincipal());
		final String token = Jwts.builder().setSubject(((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConfiguration.JWT_EXPIRATION_TIME))
				.signWith(SecurityConfiguration.JWT_AUTHENTICATION_SIG_ALG, this.authMapper.findSetting(AuthSettings.JWT_SECRET).getBytes())
				.compact();
		res.addHeader("Access-Control-Expose-Headers", SecurityConfiguration.HEADER_KEY);
		res.addHeader(SecurityConfiguration.HEADER_KEY, SecurityConfiguration.JWT_TOKEN_PREFIX + token);

	}

}