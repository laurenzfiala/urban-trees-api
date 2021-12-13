package at.sparklingscience.urbantrees.exception;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import at.sparklingscience.urbantrees.domain.EventSeverity;
import at.sparklingscience.urbantrees.security.authentication.otp.IncorrectOtpTokenException;
import at.sparklingscience.urbantrees.service.ApplicationService;

/**
 * Exception handler for the application. Creates the {@link ApiError} classes
 * to return to the user.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/26
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);
	
	@Autowired
	private ApplicationService appService;
	
	@Autowired
	private DateTimeFormatter httpHeaderDateFormatter;
	
	private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		final String error = "Malformed JSON request";
		LOGGER.debug("handleHttpMessageNotReadable: {}", error, ex);
		this.appService.logEvent(error, null, EventSeverity.SUSPICIOUS);
		return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error, ex));
	}

	@Override
	protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		final String error = "Missing path variable " + ex.getVariableName();
		LOGGER.debug("handleMissingPathVariable: {}", error, ex);
		this.appService.logEvent(error, null);
		return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error, ex));
	}

	@ExceptionHandler(NotFoundException.class)
	protected ResponseEntity<Object> handleNotFound(NotFoundException ex) {
		LOGGER.trace("handleNotFound: {}", ex.getMessage(), ex);
		ApiError apiError = new ApiError(HttpStatus.NOT_FOUND);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}
	
	@ExceptionHandler(InternalException.class)
	protected ResponseEntity<Object> handleInternal(InternalException ex) {
		LOGGER.warn("handleInternal: {}", ex.getMessage(), ex);
		ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error encountered", ex);
		this.appService.logExceptionEvent(ex);
		return buildResponseEntity(apiError);
	}
	
	/**
	 * Catches all remeinaing throwables that are not covered by the specific handlers.
	 */
	@ExceptionHandler(Throwable.class)
	protected ResponseEntity<Object> handleOtherThrowables(Throwable ex) {
		LOGGER.warn("handleInternal: {}", ex.getMessage(), ex);
		ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error encountered", ex);
		apiError.setClientErrorCodeFromClientError(ClientError.UNCAUGHT);
		this.appService.logExceptionEvent(ex);
		return buildResponseEntity(apiError);
	}
	
	@ExceptionHandler(BadRequestException.class)
	protected ResponseEntity<Object> handleBadRequest(BadRequestException ex) {
		LOGGER.trace("handleBadRequest: {}", ex.getMessage());
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		apiError.setClientErrorCodeFromClientError(ex.getClientError());
		this.appService.logExceptionEvent(ex, EventSeverity.INTERNAL);
		return buildResponseEntity(apiError);
	}
	
	@ExceptionHandler(IncorrectOtpTokenException.class)
	protected ResponseEntity<Object> handleInvalidToken(IncorrectOtpTokenException ex) {
		LOGGER.trace("handleInvalidToken: {}", ex.getMessage());
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(UnauthorizedException.class)
	protected ResponseEntity<Object> handleUnauthorized(UnauthorizedException ex) {
		LOGGER.trace("handleUnauthorized: {}", ex.getMessage());
		this.appService.logExceptionEvent(ex, EventSeverity.SUSPICIOUS);
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(TooManyRequestsException.class)
	protected ResponseEntity<Object> handleTooManyRequests(TooManyRequestsException ex) {
		LOGGER.trace("handleTooManyRequests: {}", ex.getMessage());
		this.appService.logExceptionEvent(ex, EventSeverity.SUSPICIOUS);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.RETRY_AFTER, this.httpHeaderDateFormatter.format(ex.getRetryAfter()));
		return new ResponseEntity<>(headers, HttpStatus.TOO_MANY_REQUESTS);
	}

	@ExceptionHandler(ResponseStatusException.class)
	protected ResponseEntity<Object> handleResponseStatus(ResponseStatusException ex) {
		LOGGER.trace("handleResponseStatus: {}", ex.getMessage());
		return new ResponseEntity<>(ex.getResponseHeaders(), ex.getStatus());
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getBindingResult().getFieldErrors().stream()
				.map(o -> o.getField() + ": " + o.getDefaultMessage())
				.collect(Collectors.joining(", ")));
		LOGGER.debug("handleMethodArgumentNotValid: {}, invalid arguments: {}", ex.getMessage(), apiError.getMessage());
		this.appService.logExceptionEvent(ex, EventSeverity.SUSPICIOUS);
		return buildResponseEntity(apiError);
	}
	
	@ExceptionHandler(ValidationException.class)
	protected ResponseEntity<Object> handleInvalid(ValidationException ex) {
		LOGGER.trace("handleInvalid: {}", ex.getMessage());
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		apiError.setClientErrorCodeFromClientError(ex.getClientError());
		this.appService.logExceptionEvent(ex, EventSeverity.SUSPICIOUS);
		return buildResponseEntity(apiError);
	}
	
	@ExceptionHandler(SimpleValidationException.class)
	protected ResponseEntity<Object> handleInvalidSimple(SimpleValidationException ex) {
		LOGGER.trace("handleInvalidSimple: {}", ex.getMessage());
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		apiError.setClientErrorCodeFromClientError(ex.getClientError());
		this.appService.logExceptionEvent(ex, EventSeverity.SUSPICIOUS);
		return buildResponseEntity(apiError);
	}
	
}
