package at.sparklingscience.urbantrees.domain;

import java.util.Date;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange.Range;
import at.sparklingscience.urbantrees.domain.validator.annotation.MaxFloat;
import at.sparklingscience.urbantrees.domain.validator.annotation.MinFloat;

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
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * Identifier of the associated beacon.
	 * The given dataset originates from the beacon with this id.
	 */
	@Min(1)
	private int beaconId;
	
	/**
	 * Humidity value.
	 */
	@MinFloat(0f)
	@MaxFloat(1f)
	private float humidity;
	
	/**
	 * Temperature value.
	 */
	@MinFloat(-100f)
	@MaxFloat(100f)
	private float temperature;
	
	/**
	 * Date of observation.
	 * This is actual date of check (possibly in the past,
	 * e.g. from the devices' logger).
	 */
	@DateRange(Range.PAST_AND_PRESENT)
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
