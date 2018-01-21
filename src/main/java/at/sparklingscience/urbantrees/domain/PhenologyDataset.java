package at.sparklingscience.urbantrees.domain;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange.Range;

/**
 * A single Phenology dataset.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/30
 */
public class PhenologyDataset {
	
	/**
	 * Single phenology observation with type, object and the result.
	 */
	public static class PhenologyObservation {
		
		/**
		 * The datasets' database identifier.
		 */
		@Min(value = 1, groups = {ValidationGroups.Read.class})
		private int id;
		
		/**
		 * ID of the observation type.
		 * @see #type
		 */
		@Min(1)
		private int typeId;
		
		/**
		 * Type of the observation (e.g. bark).
		 * @see #typeId
		 */
		@NotNull
		private String type;
		
		/**
		 * ID of the observation object.
		 * @see #object
		 */
		@Min(1)
		private int objectId;
		
		/**
		 * Name of the observation object (e.g. bark 1).
		 * @see #objectId
		 */
		@NotNull
		private String object;
		
		/**
		 * ID of the observation result.
		 * @see #result
		 */
		@Min(1)
		private int resultId;
		
		/**
		 * Description of the observation result (e.g. is brown).
		 * @see #resultId
		 */
		@NotNull
		private String result;
		
		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getTypeId() {
			return typeId;
		}

		public void setTypeId(int typeId) {
			this.typeId = typeId;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getObjectId() {
			return objectId;
		}

		public void setObjectId(int objectId) {
			this.objectId = objectId;
		}

		public String getObject() {
			return object;
		}

		public void setObject(String object) {
			this.object = object;
		}

		public int getResultId() {
			return resultId;
		}

		public void setResultId(int resultId) {
			this.resultId = resultId;
		}

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}
		
	}
	
	/**
	 * Phenology identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;

	/**
	 * Tree identifier of the tree this dataset corresponds to.
	 */
	@Min(1)
	private int treeId;
	
	/**
	 * Names of people who observed.
	 */
	@NotNull
	private String observers;
	
	/**
	 * List of observations made.
	 */
	@Size(min = 1)
	private List<PhenologyObservation> observations;
	
	/**
	 * Date if observation.
	 */
	@NotNull
	@DateRange(Range.PAST_AND_PRESENT)
	private Date observationDate;
	
	public PhenologyDataset() {}

	public int getTreeId() {
		return treeId;
	}

	public void setTreeId(int treeId) {
		this.treeId = treeId;
	}

	public String getObservers() {
		return observers;
	}

	public void setObservers(String observers) {
		this.observers = observers;
	}

	public List<PhenologyObservation> getObservations() {
		return observations;
	}

	public void setObservations(List<PhenologyObservation> observations) {
		this.observations = observations;
	}

	public Date getObservationDate() {
		return observationDate;
	}

	public void setObservationDate(Date observationDate) {
		this.observationDate = observationDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
