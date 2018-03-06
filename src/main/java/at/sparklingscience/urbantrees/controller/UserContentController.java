package at.sparklingscience.urbantrees.controller;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.mapper.UserContentMapper;

/**
 * Controller for storage of user-generated content.
 * 
 * @author Laurenz Fiala
 * @since 2018/02/28
 */
@RestController
@RequestMapping("/usercontent")
public class UserContentController {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(UserContentController.class);
	
	@Autowired
    private UserContentMapper userContentMapper;
	
	@RequestMapping(method = RequestMethod.POST, path = "/phenology/observation/{phenologyId:\\d+}/image")
	public void postPhenologyImage(@PathVariable int phenologyId, @RequestParam("file") MultipartFile image) {
		
		LOGGER.debug("[[ POST ]] postPhenologyObservation - phenologyId: {}", phenologyId);
		
		final String originalFilename = image.getOriginalFilename();
		if (originalFilename == null) {
			LOGGER.warn("User tried to upload file with filename: " + originalFilename);
			throw new BadRequestException("Upload filename must be set.", ClientError.PHENOLOGY_IMAGE_UPLOAD_NO_FILENAME);
		}
		
		final String fileType = image.getContentType();
		if (fileType == null || !Arrays.asList("image/jpeg", "image/png").contains(fileType)) {
			throw new BadRequestException("Upload file must be of type JPG or PNG.", ClientError.PHENOLOGY_IMAGE_UPLOAD_INVALID_TYPE);
		}
		
		try {
			this.userContentMapper.insertPhenologyImage(phenologyId, image.getBytes(), fileType);
		} catch (IOException e) {
			LOGGER.error("Could not upload phenology user image: " + e.getMessage(), e);
			throw new BadRequestException("Failed to retrieve image from client");
		}
		LOGGER.debug("[[ POST ]] postPhenologyImage |END| Successfully uploaded user image - phenologyId: {}", phenologyId);
		
	}

}
