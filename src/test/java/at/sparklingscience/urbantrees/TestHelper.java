package at.sparklingscience.urbantrees;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.executor.ResultExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used to get the properties from a single place
 * and do common logic.
 * 
 * @author Laurenz Fiala
 * @since 2017/12/31
 */
@Component
public class TestHelper {
	
	/**
	 * Jackson mapper used to convert json<->object.
	 */
	@Autowired
	ObjectMapper objectMapper;
	
	@Value("${protocol}")
	private String protocol;
	
	@Value("${host}")
	private String host;
	
	@Value("${port}")
	private String port;
	
	@Value("${endpoints.tree}")
	private String treeEndpoint;
	
	@Value("${endpoints.physiognomy}")
	private String physiognomyEndpoint;
	
	@Value("${endpoints.phenology}")
	private String phenologyEndpoint;
	
	@Value("${endpoints.beacon}")
	private String[] beaconEndpoints;
	
	@Value("${endpoints.beaconDataset}")
	private String beaconDatasetEndpoint;
	
	@Value("${security.apiKeyHeader}")
	private String apiKeyHeader;
	
	@Value("${security.apiKey}")
	private String apiKey;
	
	@Value("${at.sparklingscience.urbantrees.dateFormatPattern}")
	private String dateFormatPattern;
	
	/**
	 * Initialization.
	 */
	public TestHelper() {
		// Manually set the applications' timestamp to UTC for time conversion to work.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	/**
	 * Performs an http get request against given URI.
	 * @param typeClass The class of the object to be returned.
	 * @param uri The URI to be called.
	 * @return Type given in typeClass parameter.
	 * @throws JsonParseException If the returned payload is illegal json.
	 * @throws JsonMappingException If the returned payload can't be converted by jacksons' {@link ObjectMapper}.
	 * @throws IOException If the payload can't be read.
	 */
	public <T> T performGetRequest(Class<T> typeClass, String uri) throws JsonParseException, JsonMappingException, IOException {
		
		HttpUriRequest request = new HttpGet(uri);
		request.addHeader(this.apiKeyHeader, this.apiKey);
		
		HttpResponse response = HttpClientBuilder.create().build().execute(request);
		String payload = EntityUtils.toString(response.getEntity());
		
		return this.objectMapper.readValue(payload, typeClass);
		
	}
	
	/**
	 * Performs an http post request against given URI using given payload.
	 * @param uri The URI to be called.
	 * @param payload The requests' payload to send.
	 * @return The {@link HttpResponse} returned by the API.
	 * @throws ClientProtocolException If request fails.
	 * @throws IOException If request fails.
	 */
	public HttpResponse performPostRequest(String uri, String payload) throws ClientProtocolException, IOException {
		
		HttpPost request = new HttpPost(uri);
		request.addHeader(this.apiKeyHeader, this.apiKey);
		StringEntity entity = new StringEntity(payload);
		request.setEntity(entity);
		
		return HttpClientBuilder.create().build().execute(request);
		
	}
	
	public String getTestBasePath() {
		return this.getProtocol() + this.getHost() + ":" + this.getPort();
	}
	
	public String getProtocol() {
		return this.protocol;
	}

	public String getHost() {
		return this.host;
	}

	public String getPort() {
		return this.port;
	}

	public String getTreeEndpoint() {
		return this.getTestBasePath() + this.treeEndpoint;
	}

	public String getPhysiognomyEndpoint() {
		return this.getTestBasePath() + this.physiognomyEndpoint;
	}

	public String getPhenologyEndpoint() {
		return this.getTestBasePath() + this.phenologyEndpoint;
	}

	public List<String> getBeaconEndpoints() {
		
		List<String> endpoints = new ArrayList<>();
		for (String endpoint : this.beaconEndpoints) {
			endpoints.add(this.getTestBasePath() + endpoint);
		}
		return endpoints;
		
	}
	
	public String getBeaconDatasetEndpoint() {
		return this.getTestBasePath() + this.beaconDatasetEndpoint;
	}

	public DateFormat getDateFormatter() {
		return new SimpleDateFormat(this.dateFormatPattern);
	}

}
