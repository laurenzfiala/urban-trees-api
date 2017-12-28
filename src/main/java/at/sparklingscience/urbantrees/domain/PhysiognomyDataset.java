package at.sparklingscience.urbantrees.domain;

import java.sql.Timestamp;
import java.util.Date;

/**
 * A single Physiognomy dataset.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/27
 */
public class PhysiognomyDataset {

	private int treeId;
	
	private int treeHeight;
	
	private int trunkCircumference;
	
	private int crownBase;
	
	private int crownWidth;
	
	private Date timestamp;

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

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
}
