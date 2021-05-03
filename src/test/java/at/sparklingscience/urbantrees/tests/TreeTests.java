package at.sparklingscience.urbantrees.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import at.sparklingscience.urbantrees.domain.City;
import at.sparklingscience.urbantrees.domain.Coordinates;
import at.sparklingscience.urbantrees.domain.Location;
import at.sparklingscience.urbantrees.domain.PhenologyDataset;
import at.sparklingscience.urbantrees.domain.PhenologyObservation;
import at.sparklingscience.urbantrees.domain.PhenologyObservationObject;
import at.sparklingscience.urbantrees.domain.PhenologyObservationResult;
import at.sparklingscience.urbantrees.domain.PhysiognomyDataset;
import at.sparklingscience.urbantrees.domain.Tree;
import at.sparklingscience.urbantrees.domain.TreeGenus;
import at.sparklingscience.urbantrees.domain.TreeSpecies;

/**
 * Tests for the beacon REST endpoint.
 * 
 * @author Laurenz Fiala
 * @since 2017/01/04
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@ContextConfiguration
@Transactional(propagation = Propagation.NEVER)
public class TreeTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Value("${endpoints.tree}")
	private String treeEndpoint;

	@Value("${endpoints.physiognomy}")
	private String physiognomyEndpoint;

	@Value("${endpoints.phenology}")
	private String phenologyEndpoint;

	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;

	@Test
	public void getTree() throws ClientProtocolException, IOException {

		Tree tree = this.restTemplate.getForObject(this.treeEndpoint, Tree.class);

		Tree test = new Tree();
		test.setId(9990);
		Location testLocation = new Location();
		testLocation.setId(9990);
		testLocation.setCoordinates(new Coordinates(465.25f, 564.52f, "EPSG:900913"));
		testLocation.setStreet("Samplestreet 1");
		testLocation.setCity(new City(1, "Sample city"));
		test.setLocation(testLocation);
		test.setSpecies(new TreeSpecies(0, "Sample species", new TreeGenus(0, "Sample Genus")));
		test.setPlantationYear(2017);
		test.setPlantationYearEstimate(true);

		assertThat(tree).usingRecursiveComparison().isEqualTo(test);

	}

	@Test
	public void getTreePhysiognomy() throws ClientProtocolException, IOException, ParseException {

		PhysiognomyDataset[] datasets = this.restTemplate.getForObject(this.physiognomyEndpoint,
				PhysiognomyDataset[].class);

		PhysiognomyDataset dataset = null;
		for (PhysiognomyDataset d : datasets) {
			if (d.getId() == 9990) {
				dataset = d;
			}
		}

		PhysiognomyDataset test = new PhysiognomyDataset();
		test.setId(9990);
		test.setTreeId(9990);
		test.setTreeHeight(1005);
		test.setTrunkCircumference(150);
		test.setCrownBase(325);
		test.setCrownWidth(490);
		test.setObservationDate(new SimpleDateFormat(this.dateFormatPattern).parse("2017-01-01T00-00-00"));

		assertThat(dataset).usingRecursiveComparison().isEqualTo(test);

	}

	/*
	 * This does not work yet, since the RANDOM_PORT web env is started in a
	 * seperate thread, hence transactions are committed.
	 * 
	 * @Test public void postTreePhysiognomy() throws ClientProtocolException,
	 * IOException, ParseException {
	 * 
	 * PhysiognomyDataset test = new PhysiognomyDataset(); test.setTreeId(9990);
	 * test.setTreeHeight(1); test.setTrunkCircumference(1); test.setCrownBase(1);
	 * test.setCrownWidth(1); test.setObservationDate(new
	 * SimpleDateFormat(this.dateFormatPattern).parse("2017-01-01T01-01-01"));
	 * 
	 * PhysiognomyDataset dataset =
	 * this.restTemplate.postForObject(this.physiognomyEndpoint, test,
	 * PhysiognomyDataset.class);
	 * 
	 * // id is generated on insert test.setId(dataset.getId());
	 * 
	 * assertThat(dataset).isEqualToComparingFieldByField(test);
	 * 
	 * }
	 */

	@Test
	public void getTreePhenology() throws ClientProtocolException, IOException, ParseException {

		PhenologyDataset[] datasets = this.restTemplate.getForObject(this.phenologyEndpoint, PhenologyDataset[].class);

		PhenologyDataset dataset = null;
		for (PhenologyDataset d : datasets) {
			if (d.getId() == 9990) {
				dataset = d;
			}
		}

		PhenologyDataset test = new PhenologyDataset();
		test.setId(9990);
		test.setTreeId(9990);
		test.setObservers("Max Mustermann");
		PhenologyObservation testObservation = new PhenologyObservation();
		testObservation.setId(9990);
		testObservation.setObject(new PhenologyObservationObject(9990, "Bark 1 (Sample)", 9990));
		testObservation.setResult(new PhenologyObservationResult(9990, "is brown", 0));
		test.setObservations(Arrays.asList(testObservation));
		test.setObservationDate(new SimpleDateFormat(this.dateFormatPattern).parse("2017-01-01T00-00-00"));

		assertThat(dataset).usingRecursiveComparison().isEqualTo(test);

	}

}
