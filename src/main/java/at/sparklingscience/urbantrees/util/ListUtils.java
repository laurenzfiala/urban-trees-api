package at.sparklingscience.urbantrees.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListUtils {

	/**
	 * Adds all given elements that are not null to an unmodifiable list.
	 * @param <E> type of elements
	 * @param elements elements (may be null)
	 * @return unmodifiable list
	 */
	@SafeVarargs
	public static <E> List<E> ofNonNull(E... elements) {
        return Stream.of(elements)
	        	.filter(Objects::nonNull)
	        	.collect(Collectors.toUnmodifiableList());
    }
	
}
