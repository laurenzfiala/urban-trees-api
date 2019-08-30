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
	 * Associated user ids sent by frontend if user is not anonymous.
	 */
	private int[] observersUserIds;
	
	/**
	 * Ref to observers' user accounts.
	 * Only used if user is not anonymous.
	 */
	private Integer observersRef;
	
	/**
	 * Names of people who observed.
	 * Only used if user is anonymous.
	 */
	private String observers;
	
	/**
	 * List of observations made.
	 */
	@Size(min = 1, groups = {ValidationGroups.Update.class})
	private List<PhenologyObservation> observations;
	
	/**
	 * User remark.
	 */
	private String remark;
	
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int[] getObserversUserIds() {
		return observersUserIds;
	}

	public void setObserversUserIds(int[] observersUserIds) {
		this.observersUserIds = observersUserIds;
	}

	public Integer getObserversRef() {
		return observersRef;
	}

	public void setObserversRef(Integer observersRef) {
		this.observersRef = observersRef;
	}

}
