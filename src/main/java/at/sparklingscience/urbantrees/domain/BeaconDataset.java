package at.sparklingscience.urbantrees.domain;

import java.util.Date;

/**
 * Single dataset for a {@link Beacon}.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/30
 */
public class BeaconDataset {

	/**
	 * The datasets' database identifier.
	 */
	private int id;
	
	/**
	 * Identifier of the associated beacon.
	 * The given dataset originates from the beacon with this id.
	 */
	private int beaconId;
	
	/**
	 * Humidity value.
	 */
	private float humidity;
	
	/**
	 * Temperature value.
	 */
	private float temperature;
	
	/**
	 * Date of observation.
	 * This is actual date of check (possibly in the past,
	 * e.g. from the devices' logger).
	 */
	private Date observationDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBeaconId() {
		return beaconId;
	}

	public void setBeaconId(int beaconId) {
		this.beaconId = beaconId;
	}

	public float getHumidity() {
		return humidity;
	}

	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public Date getObservationDate() {
		return observationDate;
	}

	public void setObservationDate(Date observationDate) {
		this.observationDate = observationDate;
	}

}
