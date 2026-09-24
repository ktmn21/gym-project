package com.example.gymcrm.cucumber.context;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class ScenarioContext {

    private final Map<String, Object> values = new HashMap<>();
    private Response lastResponse;

    public void set(String key, Object value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) values.get(key);
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }

    public void setLastResponse(Response response) {
        this.lastResponse = response;
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public void clear() {
        values.clear();
        lastResponse = null;
    }
}
