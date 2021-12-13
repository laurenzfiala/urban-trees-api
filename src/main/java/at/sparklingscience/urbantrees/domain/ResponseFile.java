package at.sparklingscience.urbantrees.domain;

import java.nio.file.Path;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Holds a file path and content type to serve to the client.
 * 
 * @author Laurenz Fiala
 * @since 2021/08/01
 */
public class ResponseFile {
	
	@NotNull
	private Path path;
	
	@NotNull
	@NotEmpty
	private String type;

	public ResponseFile(@NotNull Path path, @NotNull @NotEmpty String type) {
		this.path = path;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

}
