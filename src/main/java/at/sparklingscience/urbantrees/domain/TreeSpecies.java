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
	private long id;
	
	/**
	 * Species name.
	 */
	@NotNull
	private String name;
	
	/**
	 * Genus tied to this species.
	 */
	@NotNull
	private TreeGenus genus;
	
	public TreeSpecies() {
	}
	
	public TreeSpecies(int id, String name, TreeGenus genus) {
		this.id = id;
		this.name = name;
		this.genus = genus;
	}
	
	@Override
	public int compareTo(TreeSpecies o) {
		long diff = o.getId() - this.getId();
		if (diff > 0) {
			return 1;
		} else if (diff < 0) {
			return -1;
		}
		return 0;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TreeGenus getGenus() {
		return genus;
	}

	public void setGenus(TreeGenus genus) {
		this.genus = genus;
	}
	
}
