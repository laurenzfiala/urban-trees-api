package at.sparklingscience.urbantrees.cms.component;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import at.sparklingscience.urbantrees.cms.CmsContent;
import at.sparklingscience.urbantrees.cms.CmsElement;

public class FileComponent implements CmsElement {
	
	@JsonProperty
	private Long fileUid;
	
	@JsonProperty
	private String filename;

	@Override
	public void validate(Errors errors) {
		
		if (this.fileUid == null || this.fileUid < 1l) {
			errors.rejectValue("fileUid", "File UID must be set and be least 1.");
		}
		if (this.filename == null || this.filename.length() > 255) {
			errors.rejectValue("fileUid", "Filename must be set and be at most 255 characters long.");
		}
		
	}

	public Long getFileUid() {
		return fileUid;
	}

	public void setFileUid(Long fileUid) {
		this.fileUid = fileUid;
	}
	
	/**
	 * Get all file IDs of {@link FileComponent}s in given CMS content.
	 * @param serializedCmsContent content to search
	 * @return list of file IDs contained in given content
	 */
	public static List<Long> findUidsForContent(@NotNull CmsContent cmsContent) {
		return cmsContent
				.getContent()
				.gatherAllElements()
				.stream()
				.filter(e -> e instanceof FileComponent)
				.map(e -> ((FileComponent) e).getFileUid())
				.collect(Collectors.toUnmodifiableList());
	}
	
}
