package at.sparklingscience.urbantrees.domain;

import java.util.Date;

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
	private int id;	
	
	/**
	 * The corresponding trees' identifier.
	 */
	private int treeId;
	
	/**
	 * Height of the tree in cm.
	 */
	private int treeHeight;
	
	/**
	 * Circumference of the base of the tree in cm.
	 */
	private int trunkCircumference;
	
	/**
	 * Height of the bottom-end of the trees' crown.
	 */
	private int crownBase;
	
	/**
	 * Diameter of the bottom-end of the trees' crown.
	 */
	private int crownWidth;
	
	/**
	 * Date of observation.
	 * This should be the actual date of observation,
	 * not the date of entering into the system.
	 */
	private Date observationDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
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
