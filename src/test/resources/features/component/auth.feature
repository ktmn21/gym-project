Feature: Authentication component scenarios
  As a gym CRM user
  I want to authenticate securely
  So that I can access protected resources

  @component
  Scenario: Successful login returns a JWT
    Given a registered trainee named "Alice" "Authok"
    When I login with the registered credentials
    Then the response status should be 200
    And the response should contain a JWT token

  @component
  Scenario: Login with wrong password fails
    Given a registered trainee named "Bob" "Authfail"
    When I login with username from context and password "wrong-password"
    Then the response status should be 401

  @component
  Scenario: Account is locked after three failed login attempts
    Given a registered trainee named "Carol" "Lockout"
    When I login with username from context and password "wrong-1"
    And I login with username from context and password "wrong-2"
    And I login with username from context and password "wrong-3"
    And I login with the registered credentials
    Then the response status should be 423

  @component
  Scenario: Logout blacklists the token
    Given a registered trainee named "Dave" "Logout"
    And I login with the registered credentials
    When I logout with the current token
    Then the response status should be 200
    When I request the trainee profile with the current token
    Then the response status should be 403
