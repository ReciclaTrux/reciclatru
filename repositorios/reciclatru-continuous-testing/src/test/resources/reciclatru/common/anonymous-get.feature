@ignore
Feature: Una petición sin token a una ruta protegida
  Devuelve: statusCode

  Scenario: Petición
    Given url baseUrl
    And path 'servicing-order', 'retrieval'
    When method get
    * def statusCode = responseStatus
