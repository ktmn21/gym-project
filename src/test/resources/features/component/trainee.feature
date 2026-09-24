Feature: Trainee component scenarios
  As a trainee
  I want to manage my profile
  So that my gym data stays up to date

  @component
  Scenario: Register a new trainee successfully
    When I register a trainee with first name "Emma" and last name "Trainee"
    Then the response status should be 200
    And the registration response should contain username and password

  @component
  Scenario: Trainee registration fails when first name is missing
    When I register a trainee with first name "" and last name "NoFirst"
    Then the response status should be 400

  @component
  Scenario: Owner can retrieve own trainee profile
    Given a registered trainee named "Frank" "Owner"
    And I login with the registered credentials
    When I request the trainee profile with the current token
    Then the response status should be 200
    And the profile first name should be "Frank"

  @component
  Scenario: Accessing another trainee profile is forbidden
    Given a registered trainee named "Grace" "OwnerA"
    And I login with the registered credentials
    And I store the current token as "tokenA"
    And a registered trainee named "Helen" "OwnerB"
    When I request trainee profile for username from context using token "tokenA"
    Then the response status should be 403

  @component
  Scenario: Unauthenticated profile access is rejected
    Given a registered trainee named "Ivan" "NoAuth"
    When I request the trainee profile without a token
    Then the response status should be 403
