package at.sparklingscience.urbantrees.cms;

/**
 * Holds all classes used for JSON view creation.
 * JSON vies are used to be able to recieve field in the
 * payload ({@link CmsContent}) from the frontend and persist
 * only certain properties (e.g. we don't want to persist
 * historyId, since it is saved separately in the user
 * content table).
 * 
 * @author Laurenz Fiala
 * @since 2021/02/03
 */
public class CmsContentViews {

	
	/**
	 * Used for deserialization when received from frontend.
	 */
	public static class Receive {}
	
	/**
	 * Used for serialization after CMS content was
	 * validated and before persisting it.
	 */
	public static class Persist {}
	
}
