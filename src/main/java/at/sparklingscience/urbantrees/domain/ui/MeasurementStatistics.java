package at.sparklingscience.urbantrees.domain.ui;

import java.io.Serializable;

/**
 * Statistics on measurement data to show in the UI.
 * 
 * @author Laurenz Fiala
 * @since 2019/07/14
 */
public class MeasurementStatistics implements Serializable {
	
	private static final long serialVersionUID = 20190714L;

	/**
	 * Minimum temperature of all recorded
	 * beacon datasets.
	 */
	private double beaconTempMinimum;
	
	/**
	 * Average temperature of all recorded
	 * beacon datasets.
	 */
	private double beaconTempAverage;
	
	/**
	 * Maximum temperature of all recorded
	 * beacon datasets.
	 */
	private double beaconTempMaximum;

	/**
	 * Minimum humidity of all recorded
	 * beacon datasets.
	 */
	private double beaconHumidityMinimum;
	
	/**
	 * Average humidity of all recorded
	 * beacon datasets.
	 */
	private double beaconHumidityAverage;
	
	/**
	 * Maximum humidity of all recorded
	 * beacon datasets.
	 */
	private double beaconHumidityMaximum;
	
	public double getBeaconTempMinimum() {
		return beaconTempMinimum;
	}
	
	public void setBeaconTempMinimum(double beaconTempMinimum) {
		this.beaconTempMinimum = beaconTempMinimum;
	}
	
	public double getBeaconTempAverage() {
		return beaconTempAverage;
	}
	
	public void setBeaconTempAverage(double beaconTempAverage) {
		this.beaconTempAverage = beaconTempAverage;
	}
	
	public double getBeaconTempMaximum() {
		return beaconTempMaximum;
	}
	
	public void setBeaconTempMaximum(double beaconTempMaximum) {
		this.beaconTempMaximum = beaconTempMaximum;
	}
	
	public double getBeaconHumidityMinimum() {
		return beaconHumidityMinimum;
	}
	
	public void setBeaconHumidityMinimum(double beaconHumidityMinimum) {
		this.beaconHumidityMinimum = beaconHumidityMinimum;
	}
	
	public double getBeaconHumidityAverage() {
		return beaconHumidityAverage;
	}
	
	public void setBeaconHumidityAverage(double beaconHumidityAverage) {
		this.beaconHumidityAverage = beaconHumidityAverage;
	}
	
	public double getBeaconHumidityMaximum() {
		return beaconHumidityMaximum;
	}
	
	public void setBeaconHumidityMaximum(double beaconHumidityMaximum) {
		this.beaconHumidityMaximum = beaconHumidityMaximum;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
