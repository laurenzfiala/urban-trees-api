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
	private byte[] data;
	
	@NotNull
	@NotEmpty
	private String type;
	
	@Min(1)
	private Long activateContentUid;
	
	@Min(1)
	private Long deactivateContentUid;

	private boolean active;
	
	@Min(1)
	private int userId;
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
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

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
}
