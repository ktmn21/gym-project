Feature: Training component scenarios
  As an authenticated gym user
  I want to create and delete trainings
  So that sessions are tracked in the CRM

  Background:
    Given the workload service responds successfully to workload updates
    And a registered trainee named "Nina" "Trainee"
    And I store the registered credentials as "trainee"
    And a registered trainer named "Oscar" "Trainer" with specialization id 1
    And I store the registered credentials as "trainer"

  @component
  Scenario: Authenticated user can add a training
    Given I login as stored user "trainee"
    When I add a training for trainee "trainee" and trainer "trainer" named "Morning Cardio" of type "Cardio" on "2026-03-10" lasting 60 minutes
    Then the response status should be 200

  @component
  Scenario: Adding a training without authentication is rejected
    When I add a training for trainee "trainee" and trainer "trainer" named "Secret Session" of type "Cardio" on "2026-03-11" lasting 45 minutes without a token
    Then the response status should be 403

  @component
  Scenario: Adding a training with invalid duration fails validation
    Given I login as stored user "trainee"
    When I add a training for trainee "trainee" and trainer "trainer" named "Bad Duration" of type "Cardio" on "2026-03-12" lasting 0 minutes
    Then the response status should be 400

  @component
  Scenario: Adding a training for unknown trainee returns not found
    Given I login as stored user "trainee"
    When I add a training for unknown trainee "ghost.user" and trainer "trainer" named "Missing Trainee" of type "Cardio" on "2026-03-13" lasting 30 minutes
    Then the response status should be 404
