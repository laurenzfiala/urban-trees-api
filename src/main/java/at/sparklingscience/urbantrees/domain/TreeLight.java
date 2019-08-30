package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * DAO.
 * Tree object with it's corresponding properties.
 * Not including beacons.
 * 
 * @author Laurenz Fiala
 * @since 2019/08/20
 */
public class TreeLight implements Comparable<TreeLight> {

	/**
	 * Database identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * Location information for the tree.
	 * @see Location
	 */
	@NotNull
	private Location location;
	
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
	
	@Override
	public int compareTo(TreeLight o) {
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

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
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

}
