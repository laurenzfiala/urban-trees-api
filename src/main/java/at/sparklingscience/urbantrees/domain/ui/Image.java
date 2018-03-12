package at.sparklingscience.urbantrees.domain.ui;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Generic image to display in the UI.

 * @author Laurenz Fiala
 * @since 2018/02/04
 */
public class Image implements Serializable {
	
	private static final long serialVersionUID = 20180306L;

	/**
	 * Stored as bytea in the database.
	 * Contains the image file data.
	 */
	@JsonIgnore
	private byte[] rawImageData;
	
	/**
	 * This contains the base64 encoded image data used for img-tags in the frontend.
	 */
	@NotNull
	private String encodedImage;
	
	/**
	 * Alternative text to display if the image does not load in the UI.
	 */
	@NotNull
	private String alternativeText;

	public byte[] getRawImageData() {
		return rawImageData;
	}

	public void setRawImageData(byte[] rawImageData) {
		this.rawImageData = rawImageData;
		this.setEncodedImage(rawImageData);
	}

	public String getAlternativeText() {
		return alternativeText;
	}

	public void setAlternativeText(String alternativeText) {
		this.alternativeText = alternativeText;
	}

	public String getEncodedImage() {
		return encodedImage;
	}

	/**
	 * Set the encoded image by raw byte array.
	 * Using {@link Base64Utils#encodeToString(byte[])}
	 */
	public void setEncodedImage(byte[] rawImageData) {
		this.encodedImage = Base64Utils.encodeToString(rawImageData);
	}

}
