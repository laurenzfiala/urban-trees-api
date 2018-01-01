package at.sparklingscience.urbantrees;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import at.sparklingscience.urbantrees.domain.Beacon;
import at.sparklingscience.urbantrees.domain.BeaconDataset;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BeaconTests {

	/**
	 * @see TestHelper
	 */
	@Autowired
	TestHelper helper;
	
	@Test
	public void contextLoads() {
	}
	
	@Test
	public void getBeacon() throws ClientProtocolException, IOException {
		
		for (String endpoint : this.helper.getBeaconEndpoints()) {
			
			Beacon beacon = this.helper.performGetRequest(Beacon.class, endpoint);
			
			assertEquals(9990, beacon.getId());
			assertEquals(9990, beacon.getTreeId());
			assertEquals("18-75-22-BA-6A-68", beacon.getBluetoothAddress());
			
		}
		
	}
	
	@Test
	public void getBeaconData() throws ClientProtocolException, IOException, ParseException {
		
		BeaconDataset[] datasets = this.helper.performGetRequest(
				BeaconDataset[].class,
				this.helper.getBeaconDatasetEndpoint()
				);
		
		BeaconDataset dataset = null;
		for (BeaconDataset d : datasets) {
			if (d.getId() == 9990) {
				dataset = d;
				break;
			}
		}
		assertNotNull(dataset);
		
		assertEquals(9990, dataset.getId());
		assertEquals(9990, dataset.getBeaconId());
		assertEquals(0.4f, dataset.getHumidity(), 0);
		assertEquals(17.2f, dataset.getTemperature(), 0);
		assertEquals(dataset.getObservationDate(), this.helper.getDateFormatter().parse("2017-01-01T00-00-00"));
		
	}

}
