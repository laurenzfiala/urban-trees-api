package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.NotNull;

/**
 * {@link PhenologyDataset} extension with a full tree object.
 * 
 * @author Laurenz Fiala
 * @since 2020/11/13
 */
public class PhenologyDatasetWithTree extends PhenologyDataset {
	
	/**
	 * Tree matching {@link #getTreeId()}.
	 */
	@NotNull
	private TreeLight tree;

	public TreeLight getTree() {
		return tree;
	}

	public void setTree(TreeLight tree) {
		this.tree = tree;
	}

}
