@ignore
Feature: Consulta /health
  Devuelve: statusCode

  Scenario: Salud
    Given url baseUrl
    And path 'health'
    When method get
    * def statusCode = responseStatus
