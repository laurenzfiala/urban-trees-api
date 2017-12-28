package at.sparklingscience.urbantrees.domain;

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
	private int id;
	
	/**
	 * Location information for the tree.
	 * @see TreeLocation
	 */
	private TreeLocation location;
	
	/**
	 * Trees' species.
	 */
	private String species;
	
	/**
	 * Trees' genus.
	 */
	private String genus;
	
	/**
	 * Year of plantation.
	 * May be an estimated date, check {@link #isPlantationYearEstimate}.
	 * @see #isPlantationYearEstimate
	 */
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
