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
	private long id;
	
	/**
	 * The device id given to the beacon.
	 * This consists of City-abbreviation & ascending number.
	 */
	@NotNull
	private String deviceId;
	
	/**
	 * (optional) The {@link Tree} this beacon belongs/is attached to.
	 */
	private TreeLight tree;
	
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
	
	/**
	 * Beacons' settings (only for admins)
	 */
	private BeaconSettings settings;
	
	/**
	 * Beacons' location.
	 */
	@NotNull
	private Location location;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		
		Beacon b = (Beacon) obj;
		if (this.getTree() == null ^ b.getTree() == null) {
			return false;
		}
		
		return ObjectUtils.nullSafeEquals(this.getId(), b.getId())
				&& (this.getTree() == null || b.getTree() == null || ObjectUtils.nullSafeEquals(this.getTree().getId(), b.getTree().getId()))
				&& ObjectUtils.nullSafeEquals(this.getBluetoothAddress(), b.getBluetoothAddress());
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
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

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public TreeLight getTree() {
		return tree;
	}

	public void setTree(TreeLight tree) {
		this.tree = tree;
	}

}
