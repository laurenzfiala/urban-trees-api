package at.sparklingscience.urbantrees.domain.ui;

/**
 * DTO.
 * An image to show the observations' possible results.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/10
 */
public class ObservationResultImage extends Image {

	private static final long serialVersionUID = 20180306L;

	/**
	 * ID of the associated observation result.
	 */
	private int observationResultId;
	
	/**
	 * ID of the associated tree spiecies.
	 * This is used to display different images for the results depending on the species.
	 */
	private int treeSpeciesId;
	
	public int getObservationResultId() {
		return observationResultId;
	}
	public void setObservationResultId(int observationResultId) {
		this.observationResultId = observationResultId;
	}
	public int getTreeSpeciesId() {
		return treeSpeciesId;
	}
	public void setTreeSpeciesId(int treeSpeciesId) {
		this.treeSpeciesId = treeSpeciesId;
	}
	
}
