@smoke
Feature: Salud del servicio

  Background:
    * url baseUrl

  Scenario: El servicio responde UP sin necesidad de token
    Given path 'health'
    When method get
    Then status 200
    And match header Content-Type contains 'application/json'
    And match response == { status: 'UP', version: '#string' }

  Scenario: La respuesta trae un identificador de correlación para rastrearla en los logs
    Given path 'health'
    When method get
    Then status 200
    And match header x-correlation-id == '#string'
