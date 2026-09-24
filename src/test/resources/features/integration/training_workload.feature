Feature: Gym CRM and Workload microservice integration
  As the gym CRM system
  I want training changes to notify the workload service
  So that trainer workload stays consistent across microservices

  Background:
    Given the workload service responds successfully to workload updates
    And a registered trainee named "Paula" "Integrator"
    And I store the registered credentials as "trainee"
    And a registered trainer named "Quinn" "Integrator" with specialization id 1
    And I store the registered credentials as "trainer"
    And I login as stored user "trainee"

  @integration
  Scenario: Creating a training sends ADD workload event to workload-service
    When I add a training for trainee "trainee" and trainer "trainer" named "Integration Cardio" of type "Cardio" on "2026-04-15" lasting 90 minutes
    Then the response status should be 200
    And the workload service should have received an ADD request for trainer "trainer" lasting 90 minutes on "2026-04-15"

  @integration
  Scenario: Deleting a training sends DELETE workload event to workload-service
    Given I add a training for trainee "trainee" and trainer "trainer" named "To Delete" of type "Strength" on "2026-04-16" lasting 45 minutes
    And the response status should be 200
    And I remember the last created training id for trainee "trainee"
    When I delete the remembered training
    Then the response status should be 200
    And the workload service should have received a DELETE request for trainer "trainer" lasting 45 minutes on "2026-04-16"

  @integration
  Scenario: Training still succeeds when workload-service is unavailable
    Given the workload service is unavailable
    When I add a training for trainee "trainee" and trainer "trainer" named "Resilient Session" of type "Yoga" on "2026-04-17" lasting 30 minutes
    Then the response status should be 200
