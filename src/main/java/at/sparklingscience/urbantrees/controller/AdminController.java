package at.sparklingscience.urbantrees.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.sparklingscience.urbantrees.domain.City;
import at.sparklingscience.urbantrees.domain.validator.ValidationGroups;
import at.sparklingscience.urbantrees.exception.BadRequestException;
import at.sparklingscience.urbantrees.exception.ClientError;
import at.sparklingscience.urbantrees.mapper.TreeMapper;

@RestController
@RequestMapping("/admin")
public class AdminController {
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
	
	/**
	 * String to prepend all mod_user & cre_user inserts with.
	 */
	private static final String PREPEND_USER_INSERTS = "WEBADM_";
	
	@Autowired
	private TreeMapper treeMapper;
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, path = "/city")
	public City postCity(@Validated(ValidationGroups.Update.class) @RequestBody City city, Principal principal) {
		
		LOGGER.info("[[ POST ]] postCity - city: {}", city.getName());
		
		try {
			this.treeMapper.insertCity(city, PREPEND_USER_INSERTS + principal.getName());
		} catch (DuplicateKeyException ex) {
			LOGGER.debug("Admin tried to enter duplicate city: {}", ex.getMessage(), ex);
			throw new BadRequestException("There is already a city with given name.", ClientError.CITY_DUPLICATE);
		}
		
		LOGGER.info("[[ POST ]] postCity |END| - city: {}, inserted city id: {}", city.getName(), city.getId());
		
		return city;
		
	}

}
