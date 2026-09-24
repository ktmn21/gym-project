package com.example.gymcrm.cucumber.steps;

import com.example.gymcrm.cucumber.config.CucumberSpringConfiguration;
import com.example.gymcrm.cucumber.context.ScenarioContext;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

public class CommonSteps {

    @Autowired
    private ScenarioContext context;

    @Before
    public void resetScenario() {
        context.clear();
        CucumberSpringConfiguration.workloadMock().resetAll();
        stubWorkloadSuccess();
    }

    @Given("the workload service responds successfully to workload updates")
    public void stubWorkloadSuccess() {
        CucumberSpringConfiguration.workloadMock().stubFor(
                WireMock.post(urlEqualTo("/api/workload"))
                        .willReturn(aResponse().withStatus(200)));
    }

    @Given("the workload service is unavailable")
    public void stubWorkloadUnavailable() {
        CucumberSpringConfiguration.workloadMock().resetAll();
        CucumberSpringConfiguration.workloadMock().stubFor(
                WireMock.post(urlEqualTo("/api/workload"))
                        .willReturn(aResponse().withStatus(503).withBody("Service Unavailable")));
    }

    @Then("the response status should be {int}")
    public void assertStatus(int status) {
        assertThat(context.getLastResponse().statusCode(), equalTo(status));
    }

    @Then("the response should contain a JWT token")
    public void assertJwtPresent() {
        String token = context.getLastResponse().jsonPath().getString("token");
        assertThat(token, notNullValue());
        assertThat(token.length(), greaterThan(10));
        context.set("token", token);
    }

    @Then("the registration response should contain username and password")
    public void assertRegistrationCredentials() {
        String username = context.getLastResponse().jsonPath().getString("username");
        String password = context.getLastResponse().jsonPath().getString("password");
        assertThat(username, notNullValue());
        assertThat(password, notNullValue());
        context.set("username", username);
        context.set("password", password);
    }

    @Then("the profile first name should be {string}")
    public void assertProfileFirstName(String firstName) {
        assertThat(context.getLastResponse().jsonPath().getString("firstName"), equalTo(firstName));
    }

    @Given("I store the registered credentials as {string}")
    public void storeCredentialsAs(String alias) {
        context.set(alias + ".username", context.getString("username"));
        context.set(alias + ".password", context.getString("password"));
        context.set(alias + ".firstName", context.getString("firstName"));
        context.set(alias + ".lastName", context.getString("lastName"));
    }

    @Given("I store the current token as {string}")
    public void storeTokenAs(String alias) {
        context.set(alias, context.getString("token"));
    }

    @Given("I login as stored user {string}")
    public void loginAsStoredUser(String alias) {
        String username = context.getString(alias + ".username");
        String password = context.getString(alias + ".password");
        Response response = given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .when()
                .post("/login");
        context.setLastResponse(response);
        context.set("token", response.jsonPath().getString("token"));
        context.set("username", username);
        context.set("password", password);
    }

    @Then("the workload service should have received a(n) {word} request for trainer {string} lasting {int} minutes on {string}")
    public void verifyWorkloadRequest(String actionType, String trainerAlias, int duration, String date) {
        String trainerUsername = context.getString(trainerAlias + ".username");
        CucumberSpringConfiguration.workloadMock().verify(postRequestedFor(urlEqualTo("/api/workload"))
                .withRequestBody(matchingJsonPath("$.username", com.github.tomakehurst.wiremock.client.WireMock.equalTo(trainerUsername)))
                .withRequestBody(matchingJsonPath("$.trainingDuration", com.github.tomakehurst.wiremock.client.WireMock.equalTo(String.valueOf(duration))))
                .withRequestBody(matchingJsonPath("$.trainingDate", com.github.tomakehurst.wiremock.client.WireMock.equalTo(date)))
                .withRequestBody(matchingJsonPath("$.actionType", com.github.tomakehurst.wiremock.client.WireMock.equalTo(actionType)))
                .withHeader("Authorization", containing("Bearer ")));
    }
}
