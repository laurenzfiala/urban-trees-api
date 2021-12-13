package at.sparklingscience.urbantrees.cms.component;

import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import at.sparklingscience.urbantrees.cms.CmsElement;

public class FileComponent implements CmsElement {
	
	@JsonProperty
	private Long fileUid;

	@Override
	public void validate(Errors errors) {
		
		if (this.fileUid == null || this.fileUid < 1l) {
			errors.rejectValue("fileUid", "File UID must be set and be least 1.");
		}
		
	}

	public Long getFileUid() {
		return fileUid;
	}

	public void setFileUid(Long fileUid) {
		this.fileUid = fileUid;
	}
	
}
