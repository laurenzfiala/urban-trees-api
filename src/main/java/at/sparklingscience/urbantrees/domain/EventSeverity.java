package at.sparklingscience.urbantrees.domain;

/**
 * Type of a beacon log entry.
 * 
 * @author Laurenz Fiala
 * @since 2019/07/27
 */
public enum EventSeverity {
	
	EXCEPTION(true),
	SUSPICIOUS(false),
	INTERNAL(false);
	
	private final boolean autoCreateReport;
	
	private EventSeverity(final boolean autoCreateReport) {
		this.autoCreateReport = autoCreateReport;
	}

	public boolean isAutoCreateReport() {
		return autoCreateReport;
	}

}
