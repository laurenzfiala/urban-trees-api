package at.sparklingscience.urbantrees.domain.ui;

import java.io.Serializable;

/**
 * Statistics to show in the UI,
 * aggregate data of the whole system.
 * 
 * @author Laurenz Fiala
 * @since 2018/05/23
 */
public class Statistics implements Serializable {
	
	private static final long serialVersionUID = 20180523L;

	/**
	 * Amount of cities in the system.
	 */
	private int cityAmount;
	
	/**
	 * Amount of trees in the system.
	 */
	private int treeAmount;
	
	/**
	 * Total amount of tree species.
	 */
	private int treeSpeciesAmount;
	
	/**
	 * Total amount of phenology observations.
	 */
	private long phenologyObservationAmount;
	
	/**
	 * Total amount of pehnology observations entered by users.
	 */
	private long phenologyObservationObjectAmount;
	
	/**
	 * Total amount of beacons registered.
	 */
	private long beaconAmount;
	
	/**
	 * Total amount of beacon datasets recorded.
	 */
	private long beaconDatasetAmount;

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
	
	public int getCityAmount() {
		return cityAmount;
	}
	
	public void setCityAmount(int cityAmount) {
		this.cityAmount = cityAmount;
	}
	
	public int getTreeAmount() {
		return treeAmount;
	}
	
	public void setTreeAmount(int treeAmount) {
		this.treeAmount = treeAmount;
	}
	
	public int getTreeSpeciesAmount() {
		return treeSpeciesAmount;
	}
	
	public void setTreeSpeciesAmount(int treeSpeciesAmount) {
		this.treeSpeciesAmount = treeSpeciesAmount;
	}
	
	public long getPhenologyObservationAmount() {
		return phenologyObservationAmount;
	}
	
	public void setPhenologyObservationAmount(long phenologyObservationAmount) {
		this.phenologyObservationAmount = phenologyObservationAmount;
	}
	
	public long getPhenologyObservationObjectAmount() {
		return phenologyObservationObjectAmount;
	}
	
	public void setPhenologyObservationObjectAmount(long phenologyObservationObjectAmount) {
		this.phenologyObservationObjectAmount = phenologyObservationObjectAmount;
	}
	
	public long getBeaconDatasetAmount() {
		return beaconDatasetAmount;
	}
	
	public void setBeaconDatasetAmount(long beaconDatasetAmount) {
		this.beaconDatasetAmount = beaconDatasetAmount;
	}
	
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

	public long getBeaconAmount() {
		return beaconAmount;
	}

	public void setBeaconAmount(long beaconAmount) {
		this.beaconAmount = beaconAmount;
	}
	
}
