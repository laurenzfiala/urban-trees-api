package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * DAO.
 * A city wherein a tree exists.
 * 
 * @author Laurenz Fiala
 * @since 2018/12/05
 */
public class City implements Comparable<City> {

	/**
	 * Database identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private long id;
	
	/**
	 * Genus name.
	 */
	@NotNull
	@NotEmpty
	private String name;
	
	public City() {
	}
	
	public City(long id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public int compareTo(City o) {
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

}
