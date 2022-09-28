package at.sparklingscience.urbantrees.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A wrapper for return values to add meta-info to search results.
 * 
 * @author Laurenz Fiala
 * @since 2021/02/17
 * 
 * @param <T> the type of value this filter holds
 */
public class SearchResult<T> {
	
	private UUID transactionId;
	
	/**
	 * Map of search metadata.
	 */
	private Map<String, Object> metadata = new HashMap<>();
	
	/**
	 * The actual search result.
	 */
	private T result;
	
	public SearchResult(T result) {
		this.result = result;
	}
	
	public SearchResult<T> withMetadata(String key, Object metadatum) {
		this.metadata.put(key, metadatum);
		return this;
	}
	
	public SearchResult<T> withTid(UUID transactionId) {
		this.transactionId = transactionId;
		return this;
	}

	public T getResult() {
		return result;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public UUID getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(UUID transactionId) {
		this.transactionId = transactionId;
	}
	
}
