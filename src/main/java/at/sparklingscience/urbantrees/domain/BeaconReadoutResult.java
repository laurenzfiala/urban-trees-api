package at.sparklingscience.urbantrees.domain;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * DTO for beacon datasets and settings to be sent to backend.
 * @author Laurenz Fiala
 * @since 2019/05/20
 */
public class BeaconReadoutResult {

	/**
	 * Datasets received from device.
	 */
	@NotNull
    private List<BeaconDataset> datasets;
	
	/**
	 * Beacons settings received from device.
	 */
	@NotNull
    private BeaconSettings settings;
	
	/**
	 * Time between read-out of datasets and sending of this object.
	 */
	@Min(0)
    private long timeSinceDataReadoutMs;

    public BeaconReadoutResult(List<BeaconDataset> datasets, BeaconSettings settings, long timeSinceDataReadoutMs) {
        this.datasets = datasets;
        this.settings = settings;
        this.timeSinceDataReadoutMs = timeSinceDataReadoutMs;
    }

    public List<BeaconDataset> getDatasets() {
        return datasets;
    }
    
    public void setDatasets(List<BeaconDataset> datasets) {
    	this.datasets = datasets;
    }

    public BeaconSettings getSettings() {
        return settings;
    }
    
    public void setSettings(BeaconSettings settings) {
    	this.settings = settings;
    }

	public long getTimeSinceDataReadoutMs() {
		return timeSinceDataReadoutMs;
	}

	public void setTimeSinceDataReadoutMs(long timeSinceDataReadoutMs) {
		this.timeSinceDataReadoutMs = timeSinceDataReadoutMs;
	}
}
