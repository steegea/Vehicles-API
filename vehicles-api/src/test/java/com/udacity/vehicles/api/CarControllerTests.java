package com.udacity.vehicles.api;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Condition;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.Details;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.service.CarService;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.net.URI;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Implements testing of the CarController class.
 */

//Integration testing
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CarControllerTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<Car> json;

    //Mock beans
    @MockBean
    private CarService carService;

    @MockBean
    private PriceClient priceClient;

    @MockBean
    private MapsClient mapsClient;

    @Autowired
    private TestRestTemplate testRestTemplate;

    //Variable that holds the test car instance
    private Car car;

    //ResultMatcher variables that check the results of a request
    ResultMatcher statusCodeOK = status().isOk();
    ResultMatcher contentTypeJSON = content().contentType(MediaType.APPLICATION_JSON_UTF8);
    ResultMatcher carListExists = jsonPath(".carList").exists();
    ResultMatcher carListSizeGreaterThan0 = jsonPath(".carList", hasSize(greaterThan(0)));


    /**
     * Creates pre-requisites for testing, such as an example car.
     */
    @Before
    public void setup() {

        car = getCar();
        car.setId(1L);

        /*
        BDDMockito code that calls mock (stubbed) methods and ensures their behavior
        aligns with the actual methods

        Sets pre-conditions for the "findById()" and "list()" methods
         */

        Car findByID_MockMethod = carService.findById(any(Long.class));

        given(findByID_MockMethod).willReturn(car);
        given(carService.list()).willReturn(Collections.singletonList(car));
    }

    /**
    BDDMockito code that checks the behavior of the mock "save()" method for:

    -Adding a new car OR
    -Updating an existing car

     */
    public void addOrUpdateCar(){

        Car save_MockMethod = carService.save(any(Car.class));
        given(save_MockMethod).willReturn(car);
    }

    /**
     * Tests for successful creation of new car in the system
     * @throws Exception when car creation fails in the system
     */
    @Test
    public void test1_createCar() throws Exception {

        //Method invocation
        addOrUpdateCar();

        long carID = car.getId();

        assertEquals(carID, 1);

        //Performs a POST request
        mvc.perform(
                post(new URI("/cars"))
                        .content(json.write(car).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated());


        //Mockito code to check that the "save()" method was called
        verify(carService).save(any(Car.class));

    }

    /**
     * Tests if the read operation appropriately returns a list of vehicles.
     * @throws Exception if the read operation of the vehicle list fails
     */
    @Test
    public void test2_listCars() throws Exception {

        //Performs a GET request to return all cars
        mvc.perform(get("/cars"))
                .andExpect(statusCodeOK)
                .andExpect(contentTypeJSON)
                .andExpect(carListExists)
                .andExpect(carListSizeGreaterThan0);

        //Mockito code to check that the relevant mock method was called
        verify(carService).list();

    }

    /**
     * Tests the read operation for a single car by ID.
     * @throws Exception if the read operation for a single car fails
     */
    @Test
    public void test3_findCar() throws Exception {

        //Performs a GET request to return the car with the specified ID
        mvc.perform(get("/cars/1"))
                .andExpect(statusCodeOK);

        verify(carService).findById((long) 1);

    }

    /**
     * Tests updating the details of a single car by ID.
     * @throws Exception if the update operation fails
     */
    @Test
    public void test4_updateCar() throws Exception {

        Details carDetails = car.getDetails();

        //Code for setting the car manufacturer and model
        Manufacturer manufacturer = new Manufacturer(102, "Ford");
        long carID = car.getId();

        carDetails.setManufacturer(manufacturer);
        carDetails.setModel("Focus");
        car.setCondition(Condition.NEW);

        String carManufacturerName = carDetails.getManufacturer().getName();
        int carManufacturerCode = manufacturer.getCode();

        String carModel = carDetails.getModel();

        Condition carCondition = car.getCondition();
        String carConditionString = carCondition.toString().replaceAll("<>", "");

        //Assertion statements
        assertEquals(carManufacturerCode, 102);
        assertEquals(carManufacturerName, "Ford");
        assertEquals(carModel, "Focus");
        assertEquals(carID, 1);
        assertEquals(carCondition, Condition.NEW);

        //Method invocation
        addOrUpdateCar();

        //Performs a PUT operation to update the information of the car with ID = 1
        mvc.perform(
                put("/cars/1").content(json.write(car).getJson())

                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(statusCodeOK)

                .andExpect(jsonPath("details.manufacturer.name", is(carManufacturerName)))
                .andExpect(jsonPath("details.model", is(carModel)))
                .andExpect(jsonPath("condition", containsString(carConditionString)));

        verify(carService).save(any(Car.class));


    }

    /**
     * Tests the deletion of a single car by ID.
     * @throws Exception if the delete operation of a vehicle fails
     */

    @Test
    public void test5_deleteCar() throws Exception {

        ResultMatcher statusNoContent = status().isNoContent();

        //Performs a DELETE operation to remove the car with ID = 1
        mvc.perform(delete("/cars/1"))
                .andExpect(statusNoContent);

        verify(carService).delete((long) 1);
    }


    /**
     * Creates an example Car object for use in testing.
     * @return an example Car object
     */
    private Car getCar() {
        Car car = new Car();
        car.setLocation(new Location(40.730610, -73.935242));
        Details details = new Details();
        Manufacturer manufacturer = new Manufacturer(101, "Chevrolet");
        details.setManufacturer(manufacturer);
        details.setModel("Impala");
        details.setMileage(32280);
        details.setExternalColor("white");
        details.setBody("sedan");
        details.setEngine("3.6L V6");
        details.setFuelType("Gasoline");
        details.setModelYear(2018);
        details.setProductionYear(2018);
        details.setNumberOfDoors(4);
        car.setDetails(details);
        car.setCondition(Condition.USED);
        return car;
    }
}
