package at.sparklingscience.urbantrees.domain;

import java.util.List;

/**
 * DAO.
 * Tree object with it's corresponding properties.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/25
 */
public class Tree extends TreeLight {

	/**
	 * Beacons attached to this tree.
	 */
	private List<Beacon> beacons;
	
	public List<Beacon> getBeacons() {
		return beacons;
	}

	public void setBeacons(List<Beacon> beacons) {
		this.beacons = beacons;
	}
	
}
