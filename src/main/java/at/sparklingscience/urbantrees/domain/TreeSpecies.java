package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * DAO.
 * Tree's species.
 * 
 * @author Laurenz Fiala
 * @since 2018/12/05
 */
public class TreeSpecies implements Comparable<TreeSpecies> {

	/**
	 * Database identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * Genus id tied to this species.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int genusId;
	
	/**
	 * Species name.
	 */
	@NotNull
	private String name;
	
	@Override
	public int compareTo(TreeSpecies o) {
		int diff = o.getId() - this.getId();
		if (diff > 0) {
			return 1;
		} else if (diff < 0) {
			return -1;
		}
		return 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGenusId() {
		return genusId;
	}

	public void setGenusId(int genusId) {
		this.genusId = genusId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}
