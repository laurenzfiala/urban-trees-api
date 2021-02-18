package at.sparklingscience.urbantrees.domain;

/**
 * A single filter value for a search.
 * 
 * @author Laurenz Fiala
 * @since 2021/02/17
 */
public class SearchValue {
	
	private Object value;
	
	public SearchValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value.toString();
	}

}
