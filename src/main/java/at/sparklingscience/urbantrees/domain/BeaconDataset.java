package at.sparklingscience.urbantrees.domain;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
	 * ID of associated {@link Beacon}.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int beaconId;
	
	/**
	 * Humidity value.
	 */
	@MinFloat(0f)
	@MaxFloat(1f)
	private double humidity;
	
	/**
	 * Temperature value.
	 */
	@MinFloat(-100f)
	@MaxFloat(100f)
	private double temperature;
	
	/**
	 * Dew point value.
	 */
	@MinFloat(-100f)
	@MaxFloat(100f)
	private double dewPoint;
	
	/**
	 * Date of observation.
	 * This is actual date of check (possibly in the past,
	 * e.g. from the devices' logger).
	 */
	@NotNull
	@DateRange(Range.PAST_AND_PRESENT)
	private Date observationDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getHumidity() {
		return humidity;
	}

	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public Date getObservationDate() {
		return observationDate;
	}

	public void setObservationDate(Date observationDate) {
		this.observationDate = observationDate;
	}

	public double getDewPoint() {
		return dewPoint;
	}

	public void setDewPoint(double dewPoint) {
		this.dewPoint = dewPoint;
	}

}
