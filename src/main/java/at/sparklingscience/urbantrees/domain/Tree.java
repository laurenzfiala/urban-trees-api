package at.sparklingscience.urbantrees.domain;

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
public class Tree {

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
	private String species;
	
	/**
	 * Trees' genus.
	 */
	@NotNull
	private String genus;
	
	/**
	 * Year of plantation.
	 * May be an estimated date, check {@link #isPlantationYearEstimate}.
	 * @see #isPlantationYearEstimate
	 */
	@Min(1950)
	private int plantationYear;
	
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

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
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
