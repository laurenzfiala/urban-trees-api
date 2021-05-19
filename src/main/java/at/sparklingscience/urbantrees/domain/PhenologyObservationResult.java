package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * DAO.
 * Describes the observation result and with which type/object
 * combination it is associated.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/04
 */
public class PhenologyObservationResult {

	/**
	 * Type identifier.
	 */
	@Min(1)
	private long id;
	
	/**
	 * Type name
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private String description;
	
	/**
	 * Result value.
	 */
	@NotNull
	private int value;
	
	public PhenologyObservationResult() {}
	
	public PhenologyObservationResult(long id, String description, int value) {
		this.setId(id);
		this.setDescription(description);
		this.setValue(value);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
