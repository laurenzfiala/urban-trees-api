package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * DAO.
 * Describes the observation object (e.g. Bud 1, Bud 2, Leave 1).
 * 
 * @author Laurenz Fiala
 * @since 2018/02/04
 */
public class PhenologyObservationObject {
	
	/**
	 * Type identifier.
	 */
	@Min(1)
	private int id;
	
	/**
	 * ID of the type this object belongs to.
	 */
	@NotNull
	private int typeId;

	/**
	 * Object name
	 */
	@NotNull(groups = {ValidationGroups.Read.class})
	private String name;
	
	public PhenologyObservationObject() {}
	
	public PhenologyObservationObject(int id, String name, int typeId) {
		this.setId(id);
		this.setName(name);
		this.setTypeId(typeId);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

}
