package at.sparklingscience.urbantrees.domain;

import java.util.Date;
import java.util.List;

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
		private int id;
		
		/**
		 * ID of the observation type.
		 * @see #type
		 */
		private int typeId;
		
		/**
		 * Type of the observation (e.g. bark).
		 * @see #typeId
		 */
		private String type;
		
		/**
		 * ID of the observation object.
		 * @see #object
		 */
		private int objectId;
		
		/**
		 * Name of the observation object (e.g. bark 1).
		 * @see #objectId
		 */
		private String object;
		
		/**
		 * ID of the observation result.
		 * @see #result
		 */
		private int resultId;
		
		/**
		 * Description of the observation result (e.g. is brown).
		 * @see #resultId
		 */
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
	private int id;

	/**
	 * Tree identifier of the tree this dataset corresponds to.
	 */
	private int treeId;
	
	/**
	 * Names of people who observed.
	 */
	private String observers;
	
	/**
	 * List of observations made.
	 */
	private List<PhenologyObservation> observations;
	
	/**
	 * Date if observation.
	 */
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
