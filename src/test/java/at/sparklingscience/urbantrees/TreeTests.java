package at.sparklingscience.urbantrees;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import at.sparklingscience.urbantrees.domain.PhenologyDataset;
import at.sparklingscience.urbantrees.domain.PhenologyDataset.PhenologyObservation;
import at.sparklingscience.urbantrees.domain.PhysiognomyDataset;
import at.sparklingscience.urbantrees.domain.Tree;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TreeTests {

	/**
	 * @see TestHelper
	 */
	@Autowired
	TestHelper helper;
	
	@Test
	public void getTree() throws ClientProtocolException, IOException {
		
		Tree tree = this.helper.performGetRequest(
				Tree.class, 
				this.helper.getTreeEndpoint());
		
		assertEquals(9990, tree.getId());
		assertNotNull(tree.getLocation());
		assertEquals(9990, tree.getLocation().getId());
		assertNotNull(tree.getLocation().getCoordinates());
		assertEquals(465.25f, tree.getLocation().getCoordinates().getX(), 0);
		assertEquals(564.52f, tree.getLocation().getCoordinates().getY(), 0);
		assertEquals("Samplestreet 1", tree.getLocation().getStreet());
		assertEquals("Sample city", tree.getLocation().getCity());
		assertEquals("Sample species", tree.getSpecies());
		assertEquals("Sample genus", tree.getGenus());
		assertEquals(2017, tree.getPlantationYear());
		assertTrue(tree.isPlantationYearEstimate());
		
	}
	
	@Test
	public void getTreePhysiognomy() throws ClientProtocolException, IOException, ParseException {
		
		PhysiognomyDataset[] datasets = this.helper.performGetRequest(
				PhysiognomyDataset[].class, 
				this.helper.getPhysiognomyEndpoint());
		
		PhysiognomyDataset dataset = null;
		for (PhysiognomyDataset d : datasets) {
			if (d.getId() == 9990) {
				dataset = d;
				break;
			}
		}
		assertNotNull(dataset);
		
		assertEquals(9990, dataset.getId());
		assertEquals(9990, dataset.getTreeId());
		assertEquals(1005, dataset.getTreeHeight());
		assertEquals(150, dataset.getTrunkCircumference());
		assertEquals(325, dataset.getCrownBase());
		assertEquals(490, dataset.getCrownWidth());
		assertEquals(dataset.getObservationDate(), this.helper.getDateFormatter().parse("2017-01-01T00-00-00"));
		
	}
	
	@Test
	public void getTreePhenology() throws ClientProtocolException, IOException, ParseException {
		
		PhenologyDataset[] datasets = this.helper.performGetRequest(
				PhenologyDataset[].class, 
				this.helper.getPhenologyEndpoint());
		
		PhenologyDataset dataset = null;
		for (PhenologyDataset d : datasets) {
			if (d.getId() == 9990) {
				dataset = d;
				break;
			}
		}
		assertNotNull(dataset);
		
		assertEquals(9990, dataset.getId());
		assertEquals(9990, dataset.getTreeId());
		assertEquals("Max Mustermann", dataset.getObservers());
		assertNotNull(dataset.getObservations());
		assertNotEquals(0, dataset.getObservations().size());
		PhenologyObservation obs = dataset.getObservations().get(0);
		assertEquals(9990, obs.getId());
		assertEquals(9990, obs.getTypeId());
		assertEquals("Bark (Sample)", obs.getType());
		assertEquals(9990, obs.getObjectId());
		assertEquals("Bark 1 (Sample)", obs.getObject());
		assertEquals(9990, obs.getResultId());
		assertEquals("is brown", obs.getResult());
		assertEquals(dataset.getObservationDate(), this.helper.getDateFormatter().parse("2017-01-01T00-00-00"));
		
	}
	
}
