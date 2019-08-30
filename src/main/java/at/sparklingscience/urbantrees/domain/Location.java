package at.sparklingscience.urbantrees.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;

/**
 * DAO.
 * Describes the location of a tree, beacon etc., including its coordinates, city and street.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/25
 */
public class Location {

	/**
	 * Database identifier (autoincrement).
	 */
	@Min(value = 1, groups = {ValidationGroups.Read.class})
	private int id;
	
	/**
	 * Trees' cooridinates.
	 * @see Coordinates
	 */
	@NotNull
	private Coordinates coordinates;
	
	/**
	 * Street this tree is closest to.
	 */
	@NotNull
	private String street;
	
	/**
	 * City this tree is situated.
	 */
	@NotNull
	private City city;

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

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}
	
}
