package at.sparklingscience.urbantrees.service;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.sparklingscience.urbantrees.controller.util.ControllerUtil;
import at.sparklingscience.urbantrees.domain.Event;
import at.sparklingscience.urbantrees.domain.EventSeverity;
import at.sparklingscience.urbantrees.domain.Report;
import at.sparklingscience.urbantrees.exception.InternalException;
import at.sparklingscience.urbantrees.mapper.ApplicationMapper;

/**
 * Service for application related actions like reports and event logging.
 * 
 * @author Laurenz Fiala
 * @since 2019/07/29
 */
@Service
public class ApplicationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);
	
	@Autowired
    private ApplicationMapper appMapper;
	
	@Autowired
	private ObjectMapper jsonObjectMapper;
	
	/**
	 * TODO
	 * @param message
	 * @param t
	 */
	public void logExceptionEvent(Throwable t) {
		this.logExceptionEvent(t.getClass().getName() + ": " + t.getMessage(), t);
	}
	
	/**
	 * TODO
	 * @param message
	 * @param t
	 */
	public void logExceptionEvent(String message, Throwable t) {
		this.logExceptionEvent(message, t, EventSeverity.EXCEPTION);
	}

	/**
	 * TODO
	 * @param t
	 * @param severity
	 */
	public void logExceptionEvent(Throwable t, EventSeverity severity) {
		this.logExceptionEvent(t.getClass().getName() + ": " + t.getMessage(), t, severity);
	}
	
	/**
	 * TODO
	 * @param message
	 * @param t
	 * @param severity
	 */
	public void logExceptionEvent(String message, Throwable t, EventSeverity severity) {
		String details = ControllerUtil.traceToString(t.getStackTrace());
		Throwable cause = t.getCause();
		if (cause != null) {
			details += "\n\n\tCaused by: " + cause.getMessage();
			details += "\n\t" + ControllerUtil.traceToString(cause.getStackTrace());
		}
		this.logEvent(message, details, severity);
	}
	
	/**
	 * TODO
	 * @param action
	 * @param principal
	 */
	public void logEvent(String message, String details) {
		this.logEvent(message, details, EventSeverity.INTERNAL);
	}
	
	/**
	 * TODO
	 * @param message
	 * @param details
	 * @param severity
	 */
	@Transactional
	public void logEvent(String message, String details, EventSeverity severity) {
		
		try {
			Event ev = new Event();
			ev.setMessage(message == null ? "(null)" : message);
			ev.setDetails(details);
			ev.setSeverity(severity);
			this.appMapper.insertEvent(ev);
			if (ev.getId() <= 0) {
				LOGGER.error("+++ COULD NOT LOG EVENT - INSERT FAILED +++");
				return;
			}
			if (severity.isAutoCreateReport()) {
				Report re = new Report();
				re.setMessage(severity.toString() + " EVENT: " + message);
				re.setAssocEvent(ev);
				re.setAutoCreate(true);
				this.appMapper.insertReport(re);
			}
		} catch(Throwable t) {
			LOGGER.error("+++ COULD NOT LOG EVENT - " + t.getMessage(), t);
		}
		
	}
	
	/**
	 * Get all reports for the given user.
	 */
	@Transactional
	public List<Report> getUserReports(final Authentication auth) {
		final int userId = ControllerUtil.getAuthToken(auth).getId();
		return this.appMapper.findUserReports(userId);
	}
	
	/**
	 * Enter one report into the DB.
	 */
	@Transactional
	public void report(Report report) {
		this.appMapper.insertReport(report);
	}
	
	/**
	 * Create new associated users entries in the DB and return their ref_id
	 * to insert in the used table.
	 * This is not transactional, since we only want to commit this when the rest of the
	 * transaction succeeds.
	 * @param userIds all users to associate
	 * @throws RuntimeException if any one of the DB operations fails
	 */
	public int assocUsers(int[] userIds) throws RuntimeException {
		
		int refId = this.appMapper.nextUserRefId();
		for (int uid : userIds) {
			this.appMapper.insertUserRef(refId, uid);			
		}
		return refId;
		
	}
	
	/**
	 * TODO
	 * @param <T>
	 * @param payloadObj
	 * @return
	 */
	public <T> UUID transaction(T payloadObj) {
		
		try {
			final UUID tid = UUID.randomUUID();
			final String payload = this.jsonObjectMapper.writeValueAsString(payloadObj);
			
			this.appMapper.insertTransaction(tid, payload);
			this.appMapper.deleteOldTransactions(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
			
			return tid;
		} catch (JsonProcessingException e) {
			throw new InternalException("Could not serialize object and failed to create transaction", e);
		}
		
	}
	
	/**
	 * TODO
	 * @param <T>
	 * @param transactionId
	 * @param clazz
	 * @return
	 */
	public <T> T getTransaction(UUID transactionId) {
		
		try {
			final String payload = this.appMapper.findTransaction(transactionId);
			return this.jsonObjectMapper.readValue(payload, new TypeReference<T>() {});
		} catch (JsonProcessingException e) {
			throw new InternalException("Could not deserialize transaction payload", e);
		}
		
	}

}
