package com.example.gymcrm.cucumber.steps;

import com.example.gymcrm.cucumber.context.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class TrainingSteps {

    @Autowired
    private ScenarioContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I add a training for trainee {string} and trainer {string} named {string} of type {string} on {string} lasting {int} minutes")
    public void addTraining(String traineeAlias, String trainerAlias, String name, String type, String date, int duration) {
        addTrainingInternal(resolveUsername(traineeAlias), resolveUsername(trainerAlias), name, type, date, duration, true);
    }

    @When("I add a training for trainee {string} and trainer {string} named {string} of type {string} on {string} lasting {int} minutes without a token")
    public void addTrainingWithoutToken(String traineeAlias, String trainerAlias, String name, String type, String date, int duration) {
        addTrainingInternal(resolveUsername(traineeAlias), resolveUsername(trainerAlias), name, type, date, duration, false);
    }

    @When("I add a training for unknown trainee {string} and trainer {string} named {string} of type {string} on {string} lasting {int} minutes")
    public void addTrainingUnknownTrainee(String traineeUsername, String trainerAlias, String name, String type, String date, int duration) {
        addTrainingInternal(traineeUsername, resolveUsername(trainerAlias), name, type, date, duration, true);
    }

    @Given("I remember the last created training id for trainee {string}")
    public void rememberTrainingId(String traineeAlias) {
        String traineeUsername = resolveUsername(traineeAlias);
        Long trainingId = jdbcTemplate.queryForObject(
                """
                SELECT t.id
                FROM training t
                JOIN trainee tr ON t.trainee_id = tr.id
                JOIN users u ON tr.user_id = u.id
                WHERE u.username = ?
                ORDER BY t.id DESC
                LIMIT 1
                """,
                Long.class,
                traineeUsername);
        assertThat("Expected a stored training for " + traineeUsername, trainingId, notNullValue());
        context.set("trainingId", trainingId);
    }

    @When("I delete the remembered training")
    public void deleteRememberedTraining() {
        Object trainingId = context.get("trainingId");
        Response response = given()
                .header("Authorization", "Bearer " + context.getString("token"))
                .when()
                .delete("/training/{id}", trainingId);
        context.setLastResponse(response);
    }

    private String resolveUsername(String aliasOrUsername) {
        String stored = context.getString(aliasOrUsername + ".username");
        return stored != null ? stored : aliasOrUsername;
    }

    private void addTrainingInternal(String traineeUsername, String trainerUsername, String name,
                                     String type, String date, int duration, boolean withToken) {
        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", traineeUsername);
        body.put("trainerUsername", trainerUsername);
        body.put("trainingName", name);
        body.put("trainingTypeName", type);
        body.put("trainingDate", date);
        body.put("trainingDuration", duration);

        var request = given().contentType(ContentType.JSON).body(body);
        if (withToken) {
            request = request.header("Authorization", "Bearer " + context.getString("token"));
        }
        Response response = request.when().post("/training");
        context.setLastResponse(response);
    }
}
