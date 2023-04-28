import POJO.Location;
import POJO.Place;
import POJO.User;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ZippoAndGoRestTests {

    @Test
    public void test() {


        given() // preparation : token, request body, parameters, cookies

                .when() // for url and request method (get, put, put, delete)

                .then(); // response body, assertions,

    }

    @Test
    public void statusCodeTest() {
        given()

                .when()
                .get("http://api.zippopotam.us/us/90210")
                .then()
                .log().body() // prints the RESPONSE BODY
                .log().status() // prints the STATUS CODE
                .statusCode(200); // checks status code - if it is 200
    }

    @Test
    public void contentTypeTest() {
        given()

                .when()
                .get("http://api.zippopotam.us/us/90210")
                .then()
                .log().body() // prints the RESPONSE BODY
                .statusCode(200) // checks status code - if it is 200
                .contentType(ContentType.JSON); // checks if response is in JSON FORMAT
    }

    @Test
    public void checkCountryFromResponseBody() {
        given()

                .when()
                .get("http://api.zippopotam.us/us/90210")
                .then()
                .log().body()
                .body("country", equalTo("United States"));

    }


    // **pm (postman)**                                                  // **Rest Assured**
    // pm.response.json().'post code'                                    // .body("post code")
    // pm.response.json().places[0].'place name'                         // .body("places[0].'place name'", ...)
    // ---                                                               // .body("places.'place name'" , ...) - GIVES ALL THE PLACE NAMES IN THE LIST


    @Test
    public void checkStateFromResponse() {
        given()

                .when()
                .get("http://api.zippopotam.us/us/90210")
                .then()
                .log().body()
                .body("places[0].state", equalTo("California")); // SINCE THERE IS NO SPACE IN STATE, DON'T NEED SINGLE QUOTES
    }


    @Test
    public void bodyHasItem() {
        given()
                .when()
                .get("http://api.zippopotam.us/tr/01000")
                .then()
                .log().body()
                .statusCode(200)
                .body("places.'place name'", hasItem("Büyükdikili Köyü")); // checks if any of the elements have this place name in the whole array (list)
    }

    @Test
    public void bodyArraySize() {
        given()
                .when()
                .get("http://api.zippopotam.us/us/90210")
                .then()
                .log().body()
                .statusCode(200)
                .body("places", hasSize(1)); // checks if this list's size is 1

    }

    @Test
    public void bodyArraySize2() {
        given()
                .when()
                .get("http://api.zippopotam.us/tr/01000")
                .then()
                .log().body()
                .statusCode(200)
                .body("places.'place name'", hasSize(71)); // checks if the size of the list of place names is 71
    }


    @Test
    public void multipleTest() {
        given()
                .when()
                .get("http://api.zippopotam.us/tr/01000")
                .then()
                .log().body()
                .statusCode(200)
                .body("places.'place name'", hasSize(71)) // IF YOU DON'T SPECIFY THE INDEX, YOU'LL GET AN ARRAY OF PLACE NAMES // Testing if the size of places array is 71
                .body("places.'place name'", hasItem("Büyükdikili Köyü")) // Testing if place name has this item anywhere inside the places list
                .body("places[2].'place name'", equalTo("Dörtağaç Köyü")); // Testing if the 2nd index of the places' place name is this

    }


    @Test
    public void pathParamsTest() {
        given()
                .pathParam("Country", "us")
                .pathParam("ZipCode", "90210")
                .log().uri() // to get the url
                .when()
                .get("http://api.zippopotam.us/{Country}/{ZipCode}") // you can call your pathParam like this, by putting inside {}
                .then()
                .log().body()
                .statusCode(200);
    }

    @Test
    public void pathParamsTest1() {
        // send get request for zipcodes between 90210 and 90213 verify that in all responses the size
        // of the `places` array is 1

        for (int i = 90210; i <= 90213; i++) { //creating a loop for the whole test - then we'll use the `i` for pathParam -> ZipCode

            given()
                    .pathParam("Country", "us")
                    .pathParam("ZipCode", i)
                    .log().uri() // to get the url
                    .when()
                    .get("http://api.zippopotam.us/{Country}/{ZipCode}")
                    .then()
                    .log().body()
                    .statusCode(200)
                    .body("places", hasSize(1));
        }


    }

    @Test
    public void queryParamTest() {

        given()
                .param("page", 2) //https://gorest.co.in/public/v1/users?page=2 | gets page 2 because after `?` is parameter on the url
                .when()
                .get("https://gorest.co.in/public/v1/users")
                .then()
                .log().body()
                .statusCode(200)
                .body("meta.pagination.page", equalTo(2)); // it is outer to inner
    }

    @Test
    public void queryParamTest2() {
        // send the same request above for the pages between 1-10 and check if
        // the page number we send from request and page number we get from response are the same

        for (int i = 1; i <= 10; i++) {
            given()
                    .param("page", i) // giving the `i` for the page param, so it would iterate based on the for loop
                    .when()
                    .get("https://gorest.co.in/public/v1/users")
                    .then()
//                    .log().body()
                    .statusCode(200)
                    .body("meta.pagination.page", equalTo(i)); // giving `i` to assertion for the same idea above
        }

    }


    RequestSpecification requestSpec;
    ResponseSpecification responseSpec;

    @BeforeClass
    public void setup() {
        baseURI = "https://gorest.co.in/public/v1"; // if the request url in the request method doesn't have the http part, rest assured adds baseURI in front of it


        requestSpec = new RequestSpecBuilder()
                .log(LogDetail.URI)               // prints request body
                .setContentType(ContentType.JSON) // sets the data format as JSON
                .build();

        responseSpec = new ResponseSpecBuilder()
                .expectStatusCode(200) // checks if the status coe is 200 from a responses
                .expectContentType(ContentType.JSON)  // checks of the response type is in JSON format
                .log(LogDetail.BODY)                  // prints the body of all responses
                .build();


    }


    @Test
    public void baseURITest() {
        given()
                .param("page", 2)
                .log().uri()
                .when()
                .get("/users")
                .then()
                .log().body()
                .statusCode(200)
                .body("meta.pagination.page", equalTo(2));
    }


    @Test
    public void requestResponseSpecsTest() {

        given()
                .param("page", 2)
                .spec(requestSpec) // adding this under the request so our @BeforeClass could run | IT DOESN'T WORK IF YOU DON'T ADD THE SPECS.
                .when()
                .get("/users")
                .then()
                .spec(responseSpec)// adding this under the response so our @BeforeClass could run | IT DOESN'T WORK IF YOU DON'T ADD THE SPECS.

                .body("meta.pagination.page", equalTo(2));
    }


    // **** JSON DATA EXTRACT

    @Test
    public void extractData() {


        String placeName = given()
                .pathParam("Country", "us")
                .pathParam("ZipCode", "90210")
                .log().uri() // prints the request url
                .when()
                .get("http://api.zippopotam.us/{Country}/{ZipCode}")
                .then()
//                .log().body()
                .statusCode(200)
                .extract().path("places[0].'place name'"); // with extract method, all request now returns a value
        // we can assign it to a variable like String, int, Array etc.


        System.out.println(placeName);
    }

    @Test
    public void extractData1() {

        int limit = given()
                .param("page", 2)
                .when()
                .get("/users")
                .then()
                .log().body()
                .statusCode(200)
                .extract().path("meta.pagination.limit");

        System.out.println("limit is: " + limit);
        Assert.assertEquals(limit, 10, "Test failed.");
    }

    @Test
    public void extractData2() {

        // get all ids from the response and verify that 1060492 is among them separately

        List<Integer> listOfIds =
                given()
                        .param("page", 2)
                        .when()
                        .get("/users") // baseURI gets involved again
                        .then()
//                .log().body()
                        .statusCode(200)
                        .extract().path("data.id");

        System.out.println("List of id's of page 2: " + listOfIds);
        Assert.assertTrue(listOfIds.contains(1060492), "Test failed!");
        System.out.println("Test passed!");

    }

    @Test
    public void extractData3() {

        // send get request to https://gorest.co.in/public/v1/users.
        // extract all names from data


        List<String> nameList = given()
                .when()
                .get("https://gorest.co.in/public/v1/users")
                .then()
                .statusCode(200)
                .extract().path("data.name");

        System.out.println("Names inside the data: " + nameList);
        Assert.assertEquals(nameList.get(5), "Ranjit Devar");


    }


    @Test
    public void extractData4() {


        Response response = given()
                .when()
                .get("/users")
                .then()
//                .log().body()
                .statusCode(200)
                .extract().response(); // returns the whole response

        List<Integer> listOfIds = response.path("data.id"); // we already have the whole response, we can reach or assign anything
        List<String> listOfNames = response.path("data.name");
        int limit = response.path("meta.pagination.limit");
        String currentLink = response.path("meta.pagination.links.current");

        System.out.println("List of id's: " + listOfIds);
        System.out.println("List of names: " + listOfNames);
        System.out.println("Limit: " + limit);
        System.out.println("Current link: " + currentLink);


    }


    @Test
    public void extractJsonPOJO(){
        // Location                                               // Place
          // String post code;                                      String place name;
          // String country;                                        String longitude;
          // String country abbreviation;                           String state;
          // List<Place> places;                                    String state abbreviation;
                                          //                        String latitude;

        Location location = given()

                .when()
                .get("http://api.zippopotam.us/us/90210")
                .then()
                .log().body()
                .extract().as(Location.class);

        System.out.println("location.getCountry() = " + location.getCountry());
        System.out.println("location.getPostCode() = " + location.getPostCode());
        System.out.println("location.getPlaces().get(0).getPlaceName() = " + location.getPlaces().get(0).getPlaceName());
        System.out.println("location.getPlaces().get(0).getState() = " + location.getPlaces().get(0).getState());

    }

    //extract.response() => Gives the whole response body as a Response object
    //extract.path() => We can get only one value. Doesn't allow us to assign and int to a String variable or extracting classes.
    //extract.as(Location.class) => Allows us to get the entire response body as an object. Doesn't let us separate any part of the body
    //extract.jsonPath. => Lets us set an int to a String, extract the entire body extract any part of the body we want.
                                 // So we don't have to create classes for the entire body.

    @Test
    public void extractWithJsonPath(){
        Place place = given()
                .when()
                .get("http://api.zippopotam.us/us/90210")
                .then()
                .log().body()
                .extract().jsonPath().getObject("places[0]", Place.class);

        System.out.println(place.getPlaceName());
        System.out.println(place.getStateAbbreviation());

    }

    @Test
    public void extractWithJsonPath2(){

        User user = given()
                .when()
                .get("/users")
                .then()
                .log().body()
                .statusCode(200)
                .extract().jsonPath().getObject("data[0]", User.class);

        System.out.println("user.getName() = " + user.getName());
    }




}
