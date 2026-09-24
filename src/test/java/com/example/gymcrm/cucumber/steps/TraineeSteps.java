package com.example.gymcrm.cucumber.steps;

import com.example.gymcrm.cucumber.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class TraineeSteps {

    @Autowired
    private ScenarioContext context;

    @Given("a registered trainee named {string} {string}")
    public void registerTraineeGiven(String firstName, String lastName) {
        registerTrainee(firstName, lastName);
    }

    @When("I register a trainee with first name {string} and last name {string}")
    public void registerTraineeWhen(String firstName, String lastName) {
        registerTrainee(firstName, lastName);
    }

    @When("I request the trainee profile with the current token")
    public void getOwnProfile() {
        Response response = given()
                .header("Authorization", "Bearer " + context.getString("token"))
                .when()
                .get("/trainee/{username}", context.getString("username"));
        context.setLastResponse(response);
    }

    @When("I request the trainee profile without a token")
    public void getProfileWithoutToken() {
        Response response = given()
                .when()
                .get("/trainee/{username}", context.getString("username"));
        context.setLastResponse(response);
    }

    @When("I request trainee profile for username from context using token {string}")
    public void getProfileWithOtherToken(String tokenAlias) {
        Response response = given()
                .header("Authorization", "Bearer " + context.getString(tokenAlias))
                .when()
                .get("/trainee/{username}", context.getString("username"));
        context.setLastResponse(response);
    }

    private void registerTrainee(String firstName, String lastName) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastname", lastName);
        body.put("dateOfBirth", "2000-01-15");
        body.put("address", "Test Street 1");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/trainee");
        context.setLastResponse(response);
        context.set("firstName", firstName);
        context.set("lastName", lastName);

        if (response.statusCode() == 200) {
            context.set("username", response.jsonPath().getString("username"));
            context.set("password", response.jsonPath().getString("password"));
        }
    }
}
