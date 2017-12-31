package at.sparklingscience.urbantrees.domain;

/**
 * A beacon (usually on a tree).
 * 
 * @author Laurenz Fiala
 * @since 2017/12/30
 */
public class Beacon {

	/**
	 * The beaons' database identifier.
	 */
	private int id;
	
	/**
	 * The {@link Tree} this beacon belongs/is attached to.
	 */
	private int treeId;
	
	/**
	 * The bluetooth identifier using which
	 * the beacon is identified & authenticated.
	 */
	private String bluetoothAddress;
	
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
	
}
