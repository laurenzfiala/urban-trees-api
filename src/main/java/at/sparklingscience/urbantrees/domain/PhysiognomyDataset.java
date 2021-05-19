package at.sparklingscience.urbantrees.domain;

import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange;
import at.sparklingscience.urbantrees.domain.validator.annotation.DateRange.Range;

/**
 * A single Physiognomy dataset.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/27
 */
public class PhysiognomyDataset {

	/**
	 * The datasets' database identifier.
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private long id;	
	
	/**
	 * The corresponding trees' identifier.
	 */
	@Min(1)
	private int treeId;
	
	/**
	 * Height of the tree in cm.
	 */
	@Min(200)
	@Max(2000)
	private int treeHeight;
	
	/**
	 * Circumference of the base of the tree in cm.
	 */
	@Min(1)
	@Max(400)
	private int trunkCircumference;
	
	/**
	 * Height of the bottom-end of the trees' crown.
	 */
	@Min(value = 100)
	@Max(2000)
	private int crownBase;
	
	/**
	 * Diameter of the bottom-end of the trees' crown.
	 */
	@Min(200)
	@Max(2000)
	private int crownWidth;
	
	/**
	 * Date of observation.
	 * This should be the actual date of observation,
	 * not the date of entering into the system.
	 */
	@NotNull
	@DateRange(Range.PAST_AND_PRESENT)
	private Date observationDate;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getTreeId() {
		return treeId;
	}

	public void setTreeId(int treeId) {
		this.treeId = treeId;
	}

	public int getTreeHeight() {
		return treeHeight;
	}

	public void setTreeHeight(int treeHeight) {
		this.treeHeight = treeHeight;
	}

	public int getTrunkCircumference() {
		return trunkCircumference;
	}

	public void setTrunkCircumference(int trunkCircumference) {
		this.trunkCircumference = trunkCircumference;
	}

	public int getCrownBase() {
		return crownBase;
	}

	public void setCrownBase(int crownBase) {
		this.crownBase = crownBase;
	}

	public int getCrownWidth() {
		return crownWidth;
	}

	public void setCrownWidth(int crownWidth) {
		this.crownWidth = crownWidth;
	}

	public Date getObservationDate() {
		return observationDate;
	}

	public void setObservationDate(Date observationDate) {
		this.observationDate = observationDate;
	}
	
}
