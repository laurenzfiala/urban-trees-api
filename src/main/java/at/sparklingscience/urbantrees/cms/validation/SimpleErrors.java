package at.sparklingscience.urbantrees.cms.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.Errors;

/**
 * Inspired by Spring's {@link Errors}, this class
 * is intended for occasions where, for proper validation reporting,
 * a list of nested errors is required.
 * This class does not access any fields and does not do any magic.
 * users can choose freely how to name the nested elements and where to use
 * them.
 * @author Laurenz Fiala
 * @since 2021/08/03
 */
public class SimpleErrors {
	
	/**
	 * Separates nested path segments in {@link #toString()}.
	 */
	private static final String PATH_SEPARATOR = "->";
	
	/**
	 * Name of the root object being inspected (used in {@link #toString()}).
	 */
	private final String rootObjectName;
	
	/**
	 * Current nested path.
	 */
	private final Deque<String> path = new LinkedList<>();

	/**
	 * List of reported errors.
	 */
	private final List<Error> errors = new ArrayList<>();
	
	/**
	 * Construct new instance.
	 * @param rootObjectName Name of the root object being inspected (used in {@link #toString()}).
	 */
	public SimpleErrors(String rootObjectName) {
		this.rootObjectName = rootObjectName;
	}
	
	/**
	 * Go one level deeper with given name.
	 * @param pathEl nested path element
	 */
	public void pushNestedPath(String pathEl) {
		this.path.push(pathEl);
	}
	
	/**
	 * Go one level higher and remove the last element for
	 * the current nested path.
	 */
	public void popNestedPath() {
		this.path.pop();
	}
	
	/**
	 * Add an error at the object identified by the current nested path.
	 * @param message error message
	 */
	public void reject(String message) {
		this.errors.add(new Error(this.getCurrentPath(), message));
	}

	/**
	 * Add an error at the object identified by the given child of the
	 * current nested path.
	 * @param child child affected
	 * @param message error message
	 */
	public void rejectChild(String child, String message) {
		this.pushNestedPath(child);
		this.errors.add(new Error(this.getCurrentPath(), message));
		this.popNestedPath();
	}
	
	/**
	 * @return current nested path
	 */
	private String getCurrentPath() {
		return this.path.stream()
				.sorted(Collections.reverseOrder())
				.collect(Collectors.joining(PATH_SEPARATOR));
	}
	
	/**
	 * Whether any values have been rejected or not.
	 * @return true if nothing has been rejected, false otherwise
	 */
	public boolean hasErrors() {
		return this.errors.size() > 0;
	}

	/**
	 * Get an unmodifiable list of reported errors.
	 * @return list of errors
	 */
	public List<Error> getErrors() {
		return Collections.unmodifiableList(this.errors);
	}
	
	
	@Override
	public String toString() {
		return rootObjectName + " errors:\n" + 
			   this.errors.stream()
					.map(e -> e.toString())
					.collect(Collectors.joining("\n"));
	}
	
	/**
	 * Holds a single error for a nested path.
	 */
	public static class Error {
		
		private String path;
		private String message;
		
		public Error(String path, String message) {
			this.path = path;
			this.message = message;
		}
		
		@Override
		public String toString() {
			return path + ": " + message;
		}
		
	}

}
