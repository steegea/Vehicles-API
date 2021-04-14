package com.udacity.pricing;

import com.jayway.jsonpath.JsonPath;
import com.udacity.pricing.domain.price.Price;
import com.udacity.pricing.domain.price.PriceRepository;
//import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

import net.minidev.json.JSONArray;


@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PricingServiceApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Autowired
	PriceRepository priceRepository;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	private TestRestTemplate testRestTemplate;

	/*
	Test method that utilizes features from the:

	- JSON Small and Fast Parser API:
		- https://javadoc.io/doc/net.minidev/json-smart/latest/index.html


	- Jayway JsonPath:
		https://github.com/json-path/JsonPath

	 */
	@Test
	public void testGetAllPrices() throws Exception {

		JSONArray jsonArray;
		URL getAllPricesURL = new URL("http://localhost:8082/prices");

		//Tests that the response is an HTTP OK (200)
		ResponseEntity<JSONObject> response = testRestTemplate.getForEntity("/prices/", JSONObject.class);
		assertEquals(response.getStatusCode(), HttpStatus.OK);

		/*
		Reads the JSON response from the given URL and
		checks that all the price entries are accounted for
		 */
		Object priceList =
				JsonPath.parse(getAllPricesURL)
				.read("_embedded.prices");

		jsonArray = (JSONArray) priceList;

		assertEquals(priceRepository.count(), 10);
		assertEquals(jsonArray.size(), 10);
		assertEquals(priceRepository.count(), jsonArray.size());

	}

	@Test
	public void testGetSinglePrice()  {
		ResponseEntity<Price> response = testRestTemplate.getForEntity("/prices/1", Price.class);
		assertEquals(response.getStatusCode(), HttpStatus.OK);

	}

	@Test
	public void testInvalidPrices() {

		ResponseEntity<Price> response = testRestTemplate.getForEntity("/prices/11", Price.class);
		assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);

		response = testRestTemplate.getForEntity("/prices/0", Price.class);
		assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);

	}

}
