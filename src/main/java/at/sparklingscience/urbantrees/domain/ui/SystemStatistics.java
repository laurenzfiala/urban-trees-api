package at.sparklingscience.urbantrees.domain.ui;

import java.io.Serializable;
import java.util.List;

/**
 * Statistics to show in the UI,
 * aggregate data of the whole system.
 * 
 * @author Laurenz Fiala
 * @since 2018/05/23
 */
public class SystemStatistics implements Serializable {
	
	private static final long serialVersionUID = 20190714L;

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
	 * Amount of beacon readout per day for the last 14 days.
	 */
	private List<DateIntValue> beaconReadouts;
	
	/**
	 * Amount of beacon readout per day for the last 14 days.
	 */
	private List<DateIntValue> phenologyObservations;

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
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public long getBeaconAmount() {
		return beaconAmount;
	}

	public void setBeaconAmount(long beaconAmount) {
		this.beaconAmount = beaconAmount;
	}

	public List<DateIntValue> getBeaconReadouts() {
		return beaconReadouts;
	}

	public void setBeaconReadouts(List<DateIntValue> beaconReadouts) {
		this.beaconReadouts = beaconReadouts;
	}

	public List<DateIntValue> getPhenologyObservations() {
		return phenologyObservations;
	}

	public void setPhenologyObservations(List<DateIntValue> phenologyObservations) {
		this.phenologyObservations = phenologyObservations;
	}
	
}
