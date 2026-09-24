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

public class AuthSteps {

    @Autowired
    private ScenarioContext context;

    @When("I login with the registered credentials")
    public void loginWithRegisteredCredentials() {
        login(context.getString("username"), context.getString("password"));
    }

    @When("I login with username from context and password {string}")
    public void loginWithContextUsername(String password) {
        login(context.getString("username"), password);
    }

    @When("I logout with the current token")
    public void logout() {
        Response response = given()
                .header("Authorization", "Bearer " + context.getString("token"))
                .when()
                .post("/logout");
        context.setLastResponse(response);
    }

    private void login(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/login");
        context.setLastResponse(response);
        if (response.statusCode() == 200) {
            context.set("token", response.jsonPath().getString("token"));
        }
    }
}
