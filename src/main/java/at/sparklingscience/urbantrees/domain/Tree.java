package at.sparklingscience.urbantrees.domain;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * DAO.
 * Tree object with it's corresponding properties.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/25
 */
public class Tree implements Comparable<Tree> {

	/**
	 * Database identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * Location information for the tree.
	 * @see TreeLocation
	 */
	@NotNull
	private TreeLocation location;
	
	/**
	 * Trees' species.
	 */
	@NotNull
	private TreeSpecies species;
	
	/**
	 * Year of plantation.
	 * May be an estimated date, check {@link #isPlantationYearEstimate}.
	 * @see #isPlantationYearEstimate
	 */
	@Min(1950)
	private int plantationYear;
	
	/**
	 * Beacons attached to this tree.
	 */
	private List<Beacon> beacons;
	
	@Override
	public int compareTo(Tree o) {
		int diff = o.getId() - this.getId();
		if (diff > 0) {
			return 1;
		} else if (diff < 0) {
			return -1;
		}
		return 0;
	}
	
	/**
	 * Whether {@link #plantationYear} is estimated or known.
	 * @see #plantationYear
	 */
	private boolean isPlantationYearEstimate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TreeLocation getLocation() {
		return location;
	}

	public void setLocation(TreeLocation location) {
		this.location = location;
	}
	
	public TreeSpecies getSpecies() {
		return species;
	}

	public void setSpecies(TreeSpecies species) {
		this.species = species;
	}
	
	public int getPlantationYear() {
		return plantationYear;
	}

	public void setPlantationYear(int plantationYear) {
		this.plantationYear = plantationYear;
	}

	public boolean isPlantationYearEstimate() {
		return isPlantationYearEstimate;
	}

	public void setPlantationYearEstimate(boolean isPlantationYearEstimate) {
		this.isPlantationYearEstimate = isPlantationYearEstimate;
	}

	public List<Beacon> getBeacons() {
		return beacons;
	}

	public void setBeacons(List<Beacon> beacons) {
		this.beacons = beacons;
	}
	
}
