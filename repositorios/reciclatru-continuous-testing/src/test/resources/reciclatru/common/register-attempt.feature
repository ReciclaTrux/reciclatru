@ignore
Feature: Un intento de registro (con datos válidos y único)
  Argumentos: n
  Devuelve: statusCode

  Scenario: Intento
    * def email = 'karate.rate.' + java.util.UUID.randomUUID().toString().substring(0, 12) + '@example.com'
    Given url baseUrl
    And path 'party-data-management', 'initiation'
    And request { email: '#(email)', password: '#(testPassword)', role: 'NEIGHBOR' }
    When method post
    * def statusCode = responseStatus
