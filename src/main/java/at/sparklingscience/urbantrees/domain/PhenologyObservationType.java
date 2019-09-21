package at.sparklingscience.urbantrees.domain;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * DAO.
 * Describes the observation type (e.g. Buds, Leaves).
 * 
 * @author Laurenz Fiala
 * @since 2018/02/04
 */
public class PhenologyObservationType {

	/**
	 * Type identifier.
	 */
	@Min(1)
	private int id;
	
	/**
	 * Type name
	 */
	@NotNull
	private String name;
	
	/**
	 * Whether the type is optional to fill out or not.
	 */
	private boolean optional;
	
	@NotNull
	private List<PhenologyObservationObject> objects;
	
	private List<PhenologyObservationResult> results;
	
	public PhenologyObservationType() {}
	
	public PhenologyObservationType(int id, String name, boolean optional) {
		this.setId(id);
		this.setName(name);
		this.setOptional(optional);
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

	public List<PhenologyObservationObject> getObjects() {
		return objects;
	}

	public void setObjects(List<PhenologyObservationObject> objects) {
		this.objects = objects;
	}

	public List<PhenologyObservationResult> getResults() {
		return results;
	}

	public void setResults(List<PhenologyObservationResult> results) {
		this.results = results;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

}
