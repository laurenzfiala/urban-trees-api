package at.sparklingscience.urbantrees.domain;

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
	private float x;
	
	/**
	 * Y-Coordinate
	 */
	private float y;
	
	public Coordinates() {}
	
	public Coordinates(float x, float y) {
		this.setX(x);
		this.setY(y);
	}
	
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
		
}
