package at.sparklingscience.urbantrees.cms.layout;

import java.util.List;

import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import at.sparklingscience.urbantrees.cms.CmsElement;
import at.sparklingscience.urbantrees.cms.component.ImageComponent;
import at.sparklingscience.urbantrees.util.ListUtils;

public class ExpDaysLayout implements CmsElement {

	@JsonProperty
	private Integer rating;

	@JsonProperty
	private Integer favouriteExperimentIndex;

	@JsonProperty
	private String learnedDescription;

	@JsonProperty
	private ImageComponent picture;

	@Override
	public void validate(Errors errors) {
		
		if (this.rating == null) {
			errors.rejectValue("rating", "Rating may not be null");
		}
		if (this.favouriteExperimentIndex == null) {
			errors.rejectValue("favouriteExperimentIndex", "FavouriteExperimentIndex may not be null");
		}
		if (this.picture != null) {
			errors.pushNestedPath("picture");
			this.picture.validate(errors);
			errors.popNestedPath();
		}
		
	}
	
	@Override
	public void sanitize() {}

	@Override
	public List<CmsElement> getChildren() {
		return ListUtils.ofNonNull(this.picture);
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public Integer getFavouriteExperimentIndex() {
		return favouriteExperimentIndex;
	}

	public void setFavouriteExperimentIndex(Integer favouriteExperimentIndex) {
		this.favouriteExperimentIndex = favouriteExperimentIndex;
	}

	public String getLearnedDescription() {
		return learnedDescription;
	}

	public void setLearnedDescription(String learnedDescription) {
		this.learnedDescription = learnedDescription;
	}

	public ImageComponent getPicture() {
		return picture;
	}

	public void setPicture(ImageComponent picture) {
		this.picture = picture;
	}
	
}
