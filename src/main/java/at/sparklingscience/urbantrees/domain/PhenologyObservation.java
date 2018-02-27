package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * Single phenology observation with type, object and the result.
 */
public class PhenologyObservation {

	/**
	 * The datasets' database identifier.
	 */
	@Min(value = 1, groups = { ValidationGroups.Read.class })
	private int id;
	
	/**
	 * Observed object.
	 */
	@NotNull
	private PhenologyObservationObject object;

	/**
	 * Observation result.
	 */
	@NotNull
	private PhenologyObservationResult result;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public PhenologyObservationObject getObject() {
		return object;
	}

	public void setObject(PhenologyObservationObject object) {
		this.object = object;
	}

	public PhenologyObservationResult getResult() {
		return result;
	}

	public void setResult(PhenologyObservationResult result) {
		this.result = result;
	}

}