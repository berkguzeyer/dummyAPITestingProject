import POJO.User;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class GoRestUsersTest {

    public String createRandomName() {
        return RandomStringUtils.randomAlphabetic(8); // this method creates a random String that's specified number long (e.g. 8 for this)
    }

    public String createRandomEmail() {
        return RandomStringUtils.randomAlphabetic(8).toLowerCase() + "@techno.com";
    }

    RequestSpecification requestSpec;
    ResponseSpecification responseSpec;

    @BeforeClass
    public void setup() {
        baseURI = "https://gorest.co.in/public/v2/users";


        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer e3d58e57e5937142a4702522dc74fc255ffe7ce6416c7e93637ab2998a1251a3")
                .build();

        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .log(LogDetail.BODY)
                .build();

    }

    @Test(enabled = false)
    public void createAUser() {
        given()
                .spec(requestSpec)
                .body("{\"name\":\"Brian Doe\",\"email\":\"briantest55511@test.com\",\"gender\": \"male\",\"status\": \"active\"}") // all JSON format is between {}
                .log().uri()
                .log().body()
                .when()
                .post()


                .then()
                .spec(responseSpec)
                .statusCode(201);

    }

    @Test(enabled = false)
    public void createUserWithRandomMethods() { // FIRST WAY TO ADD A NEW DATA

        given()   // preparation (headers, parameters...)
                .spec(requestSpec)
                .body("{\"name\":\"" + createRandomName() + "\",\"email\":\"" + createRandomEmail() + "\",\"gender\": \"male\",\"status\": \"active\"}")
                .log().uri()
                .log().body()

                .when()
                .post("https://gorest.co.in/public/v2/users")

                .then()
                .spec(responseSpec)
                .statusCode(201);

    }

    @Test(enabled = false)
    public void createAUserWithMaps() { // SECOND WAY TO ADD A NEW DATA -> Using Map

        Map<String, String> user = new HashMap<>(); // since Map is an interface and can't create an object, you should specify which Map you're using like HashMap

        user.put("name", createRandomName()); // you should use the names (json) for the key
        user.put("email", createRandomEmail());
        user.put("gender", "male");
        user.put("status", "active");

        given()
                .spec(requestSpec)
                .body(user)
                .log().uri()
                .log().body()

                .when()
                .post("https://gorest.co.in/public/v2/users") //using POST to create a new data

                .then()
                .spec(responseSpec)
                .statusCode(201);


    }


    Response response;
    User user;

    @Test
    public void createAUserWithObjects() { // You don't need to put priority on this, since all methods already depend on this one.


        user = new User();
        user.setName(createRandomName());
        user.setEmail(createRandomEmail());
        user.setGender("male");
        user.setStatus("active");

        response = given()
                .spec(requestSpec)
                .body(user)
                .log().uri()
                .log().body()

                .when()
                .post() //using POST to create a new data | it's getting baseURI

                .then()
                .spec(responseSpec)
                .statusCode(201)
                .extract().response();


    }

    /**
     * Write create user negative test
     **/


    @Test(dependsOnMethods = "createAUserWithObjects", priority = 1) // Key part here is the `dependsOnMethods`
    public void createUserNegativeTest() {


        user = new User();
        user.setName(createRandomName());
        user.setEmail(response.path("email")); // this is the important part.
        user.setGender("male");
        user.setStatus("active");

        given()
                .spec(requestSpec)
                .body(user)
                .log().body()
                .when()
                .post() //using POST to create a new data | it's getting baseURI
                .then()
                .spec(responseSpec)
                .statusCode(422)
                .body("[0].message", equalTo("has already been taken")); // Since it's an array, you should specify the index to reach the message.


    }

    /**
     * get the user you created in createAUserWithObjects test by id
     **/

    @Test(dependsOnMethods = "createAUserWithObjects", priority = 2)
    public void getUserWithId() {

        given()
                .spec(requestSpec)
                .when()
                .get("/" + response.path("id"))
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("id", equalTo(response.path("id")));
    }

    @Test(dependsOnMethods = "createAUserWithObjects", priority = 3)
    public void getUserWithId2() {

        given()
                .spec(requestSpec)
                .pathParam("userId", response.path("id"))
                .when()
                .get("/{userId}")
                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("name", equalTo(response.path("name")))
                .body("email", equalTo(response.path("email")))
                .body("id", equalTo(response.path("id")));

    }

    /**
     * Update the user you created in createAUserWithObjects
     **/
    @Test(dependsOnMethods = "createAUserWithObjects", priority = 4)
    public void updateCreatedUser() {

        user.setName("Brian");

        given()
                .spec(requestSpec)
                .body(user) // if you want to change anything, you do it like this.
                .pathParam("userId", response.path("id"))
                .when()
                .put("/{userId}")
                .then()
                .spec(responseSpec)
                .statusCode(200);
    }


    /**
     * Delete the user we created in createAUserWithObjects
     **/

    @Test(dependsOnMethods = "createAUserWithObjects", priority = 5)
    public void deleteUser() {
        given()
                .spec(requestSpec)
                .pathParam("userId", response.path("id"))
                .when()
                .delete("/{userId}")
                .then()
                .statusCode(204);
    }

    /**
     * Create a delete user negative test
     **/

    @Test(dependsOnMethods = {"createAUserWithObjects", "deleteUser"}, priority = 6)
    // This will depend on 2 methods that's why we declare 2 methods.
    public void negativeDeleteUser() {
        given()
                .spec(requestSpec)
                .pathParam("userId", response.path("id"))
                .when()
                .delete("/{userId}")
                .then()
                .statusCode(404);
    }

    @Test
    public void getUsers() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get()

                .then()
                .spec(responseSpec)
                .statusCode(200)
                .extract().response();

        int userId0 = response.jsonPath().getInt("[0].id");
        int userId3 = response.jsonPath().getInt("[2].id");

        List<User> usersList = response.jsonPath().getList("", User.class); //using User.class because you are creating a User object (List<User>)

        System.out.println("userId0 = " + userId0);
        System.out.println("userId3 = " + userId3);
        System.out.println("usersList = " + usersList); // making sure to have toString method in the User class


    }


}
