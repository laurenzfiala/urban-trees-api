package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Holds a single user content file.
 * 
 * @author Laurenz Fiala
 * @since 2020/12/22
 */
public class UserContentFile {
	
	@Min(1)
	private long id;
	
	@NotNull
	private String contentPath;
	
	@NotNull
	private String path;
	
	@NotNull
	@NotEmpty
	private String type;
	
	@Min(1)
	private Long activateContentUid;
	
	@Min(1)
	private Long deactivateContentUid;

	private boolean active;
	
	@Min(1)
	private Integer userId;

	public UserContentFile() {}
	
	public UserContentFile(String contentPath, String path, String type, Integer userId) {
		this.contentPath = contentPath;
		this.path = path;
		this.type = type;
		this.userId = userId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getActivateContentUid() {
		return activateContentUid;
	}

	public void setActivateContentUid(Long activateContentUid) {
		this.activateContentUid = activateContentUid;
	}

	public Long getDeactivateContentUid() {
		return deactivateContentUid;
	}

	public void setDeactivateContentUid(Long deactivateContentUid) {
		this.deactivateContentUid = deactivateContentUid;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getContentPath() {
		return contentPath;
	}

	public void setContentPath(String contentPath) {
		this.contentPath = contentPath;
	}
	
}
