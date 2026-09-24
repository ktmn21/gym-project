Feature: Trainer component scenarios
  As a trainer
  I want to manage my profile
  So that trainees can find me

  @component
  Scenario: Register a new trainer successfully
    When I register a trainer with first name "Julia" last name "Coach" and specialization id 1
    Then the response status should be 200
    And the registration response should contain username and password

  @component
  Scenario: Trainer registration fails for unknown specialization
    When I register a trainer with first name "Kyle" last name "Coach" and specialization id 9999
    Then the response status should be 404

  @component
  Scenario: Trainer registration fails when last name is missing
    When I register a trainer with first name "Laura" last name "" and specialization id 1
    Then the response status should be 400

  @component
  Scenario: Owner can retrieve own trainer profile
    Given a registered trainer named "Mike" "Mentor" with specialization id 1
    And I login with the registered credentials
    When I request the trainer profile with the current token
    Then the response status should be 200
    And the profile first name should be "Mike"
