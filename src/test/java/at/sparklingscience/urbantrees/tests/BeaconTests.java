package at.sparklingscience.urbantrees.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.http.client.ClientProtocolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import at.sparklingscience.urbantrees.domain.Beacon;
import at.sparklingscience.urbantrees.domain.BeaconDataset;
import at.sparklingscience.urbantrees.domain.Tree;

/**
 * Tests for the beacon REST endpoint.
 * 
 * @author Laurenz Fiala
 * @since 2017/01/04
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@ContextConfiguration
public class BeaconTests {

	@Autowired
	private TestRestTemplate restTemplate;
	
	@Value("${endpoints.beaconById}")
	private String beaconByIdEndpoint;
	
	@Value("${endpoints.beaconByAddress}")
	private String beaconByAddressEndpoint;
	
	@Value("${endpoints.beaconData}")
	private String beaconDataEndpoint;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
	@Test
	public void getBeaconById() throws ClientProtocolException, IOException {
		
		Beacon beacon = this.restTemplate.getForObject(this.beaconByIdEndpoint, Beacon.class);
		
		Beacon test = new Beacon();
		test.setId(9990);
		test.setTree(new Tree());
		test.getTree().setId(9990);
		test.setBluetoothAddress("18-75-22-BA-6A-68");
		
		assertThat(beacon).isEqualToComparingFieldByField(test);
		
	}
	
	@Test
	public void getBeaconByAddress() throws ClientProtocolException, IOException {
		
		Beacon beacon = this.restTemplate.getForObject(this.beaconByAddressEndpoint, Beacon.class);
		
		Beacon test = new Beacon();
		test.setId(9990);
		test.setTree(new Tree());
		test.getTree().setId(9990);
		test.setBluetoothAddress("18-75-22-BA-6A-68");
		
		assertThat(beacon).isEqualToComparingFieldByField(test);
		
	}
	
	@Test
	public void getBeaconData() throws ClientProtocolException, IOException, ParseException {
		
		BeaconDataset[] datasets = this.restTemplate.getForObject(this.beaconDataEndpoint, BeaconDataset[].class);
		
		BeaconDataset dataset = null;
		for (BeaconDataset d : datasets) {
			if (d.getId() == 9990) {
				dataset = d;
			}
		}
		
		BeaconDataset test = new BeaconDataset();
		test.setId(9990);
		test.setHumidity(0.4f);
		test.setTemperature(17.2f);
		test.setObservationDate(new SimpleDateFormat(this.dateFormatPattern).parse("2017-01-01T00-00-00"));
		
		assertThat(dataset).isEqualToComparingFieldByField(test);
		
	}

}
