package at.sparklingscience.urbantrees.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * A user role.
 * 
 * @author Laurenz Fiala
 * @since 2019/01/31
 */
public class Role implements Serializable {

	private static final long serialVersionUID = 20190131L;

	/**
	 * Roles' unique identifier.
	 */
	@Min(value = 1)
	private int id;
	
	/**
	 * Roles' name.
	 */
	@NotNull
	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		return id == other.id && Objects.equals(name, other.name);
	}
	
}
