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

public class TrainerSteps {

    @Autowired
    private ScenarioContext context;

    @Given("a registered trainer named {string} {string} with specialization id {int}")
    public void registerTrainerGiven(String firstName, String lastName, int specializationId) {
        registerTrainer(firstName, lastName, specializationId);
    }

    @When("I register a trainer with first name {string} last name {string} and specialization id {long}")
    public void registerTrainerWhen(String firstName, String lastName, long specializationId) {
        registerTrainer(firstName, lastName, specializationId);
    }

    @When("I request the trainer profile with the current token")
    public void getOwnTrainerProfile() {
        Response response = given()
                .header("Authorization", "Bearer " + context.getString("token"))
                .when()
                .get("/trainer/{username}", context.getString("username"));
        context.setLastResponse(response);
    }

    private void registerTrainer(String firstName, String lastName, long specializationId) {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("specializationId", specializationId);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/trainer");
        context.setLastResponse(response);
        context.set("firstName", firstName);
        context.set("lastName", lastName);

        if (response.statusCode() == 200) {
            context.set("username", response.jsonPath().getString("username"));
            context.set("password", response.jsonPath().getString("password"));
        }
    }
}
