package at.sparklingscience.urbantrees.domain;

/**
 * Summarized status of a beacon.
 * This is supposed to help quickly see issues with the beacon.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/16
 */
public enum BeaconStatus {

	/**
	 * The beacon is operational and connects & logs normally.
	 */
	OK,
	
	/**
	 * The beacon has an unknown PIN & needs to be reset.
	 */
	LOCKED,
	
	/**
	 * The beacon has invalid settings which prevent correct
	 * read-out of data or logs.
	 */
	INVALID_SETTINGS
	
}
