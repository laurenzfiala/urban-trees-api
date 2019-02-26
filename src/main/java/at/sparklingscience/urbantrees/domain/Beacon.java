package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.util.ObjectUtils;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * A beacon (usually on a tree).
 * Only holds necessary properties for display.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/30
 */
public class Beacon {

	/**
	 * The beaons' database identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * The device id given to the beacon.
	 * This consists of City-abbreviation & ascending number.
	 */
	@NotNull
	private String deviceId;
	
	/**
	 * The {@link Tree} this beacon belongs/is attached to.
	 */
	@Min(1)
	private int treeId;
	
	/**
	 * The bluetooth identifier using which
	 * the beacon is identified & authenticated.
	 */
	@NotNull
	private String bluetoothAddress;
	
	/**
	 * Beacons' status.
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private BeaconStatus status;
	
	private BeaconSettings settings;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		
		Beacon b = (Beacon) obj;
		
		return ObjectUtils.nullSafeEquals(this.getId(), b.getId())
				&& ObjectUtils.nullSafeEquals(this.getTreeId(), b.getTreeId())
				&& ObjectUtils.nullSafeEquals(this.getBluetoothAddress(), b.getBluetoothAddress());
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getTreeId() {
		return treeId;
	}
	
	public void setTreeId(int treeId) {
		this.treeId = treeId;
	}
	
	public String getBluetoothAddress() {
		return bluetoothAddress;
	}
	
	public void setBluetoothAddress(String bluetoothAddress) {
		this.bluetoothAddress = bluetoothAddress;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public BeaconStatus getStatus() {
		return status;
	}

	public void setStatus(BeaconStatus status) {
		this.status = status;
	}

	public BeaconSettings getSettings() {
		return settings;
	}

	public void setSettings(BeaconSettings settings) {
		this.settings = settings;
	}

}
