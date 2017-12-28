package at.sparklingscience.urbantrees.domain;

/**
 * DAO.
 * Describes the location of a tree, including its coordinates, city and street.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/25
 */
public class TreeLocation {

	/**
	 * Database identifier (autoincrement).
	 */
	private int id;
	
	/**
	 * Trees' cooridinates.
	 * @see Coordinates
	 */
	private Coordinates coordinates;
	
	/**
	 * Street this tree is closest to.
	 */
	private String street;
	
	/**
	 * City this tree is situated.
	 */
	private String city;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Coordinates getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
	
}
