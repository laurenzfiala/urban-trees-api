package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * DAO.
 * Describes a location via x and y coordinates.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/25
 */
public class Coordinates {

	/**
	 * X-Coordinate
	 */
	@Min(0)
	private double x;
	
	/**
	 * Y-Coordinate
	 */
	@Min(0)
	private double y;
	
	/**
	 * Y-Coordinate
	 */
	@NotNull(groups = ValidationGroups.Read.class)
	private String projection;
	
	public Coordinates() {}
	
	public Coordinates(double x, double y, String projection) {
		this.setX(x);
		this.setY(y);
		this.setProjection(projection);
	}
	
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}

	public String getProjection() {
		return projection;
	}

	public void setProjection(String projection) {
		this.projection = projection;
	}
		
}
