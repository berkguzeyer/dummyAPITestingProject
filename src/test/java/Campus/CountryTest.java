package Campus;

import Campus.Models.Country;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CountryTest {

    public String randomCountryName() {
        return RandomStringUtils.randomAlphabetic(8);
    }

    public String randomCode() {
        return RandomStringUtils.randomAlphabetic(4);
    }


    Cookies cookies;


    @BeforeClass
    // I am having my login function as a BeforeClass because it logs in and gets me the cookies, credentials etc.
    // I also won't need dependsOn method anymore
    public void login() {
        baseURI = "https://test.mersys.io"; // I will declare my baseURI here since it is BeforeClass


        Map<String, String> credentials = new HashMap<>(); // Creating a Map first to send the credentials in the request body as a Map
        credentials.put("username", "turkeyts");
        credentials.put("password", "TechnoStudy123");
        credentials.put("rememberMe", "true");


        cookies = given()
                .body(credentials) //sending credentials as request body
                .contentType(ContentType.JSON) //setting the content type to json
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .log().body()
                .extract().response().getDetailedCookies(); //extracting cookies from the response

    }


    Country country;
    String countryId;
    Response response;

    @Test
    public void createCountry() {

        // Since my Country class already matches the names with the json format data, I don't have to specify which values I'm declaring.
        // I just use set methods to send the values into the certain data.

        country = new Country();
        country.setName(randomCountryName());
        country.setCode(randomCode());

        response = given()
                .body(country)
                .contentType(ContentType.JSON)
                .cookies(cookies) // you have to make this Test depend on the loginTest to get the cookies.

                .when()
                .post("/school-service/api/countries")

                .then()
                .log().body()
                .statusCode(201)
                .extract().response();


    }

    @Test(dependsOnMethods = "createCountry", priority = 1)
    public void createCountryNegativeTest() {


        given()
                .body(country)
                .contentType(ContentType.JSON)
                .cookies(cookies)

                .when()
                .post("/school-service/api/countries")
                .then()
                .statusCode(400);

    }

    @Test(dependsOnMethods = "createCountry", priority = 2)
    public void updateCountry() {

        country.setId(response.jsonPath().getString("id")); // This is how I'm reaching to the specific country - by id. And sending it with the req body.
        country.setName(randomCountryName());
        country.setCode(randomCode());

        given()
                .body(country)
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when()
                .put("/school-service/api/countries")
                .then()
                .statusCode(200);
    }


    @Test(dependsOnMethods = "createCountry", priority = 3)
    public void deleteCountry() {

        given()
                .cookies(cookies)
                .pathParam("countryId", response.jsonPath().getString("id"))
                .when()
                .delete("/school-service/api/countries/{countryId}")
                .then()
                .statusCode(200);

    }

    @Test(dependsOnMethods = {"createCountry", "deleteCountry"}, priority = 4)
    public void deleteCountryNegativeTest() {

        given()
                .cookies(cookies)
                .pathParam("countryId", response.jsonPath().getString("id"))
                .when()
                .delete("/school-service/api/countries/{countryId}")
                .then()
                .statusCode(400);

    }


}
