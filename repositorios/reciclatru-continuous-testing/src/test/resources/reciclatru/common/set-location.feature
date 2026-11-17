@ignore
Feature: Fija el punto de recojo de un vecino
  Argumentos: token, address, district, lat, lng

  Scenario: Ubicación
    Given url baseUrl
    And path 'location-data-management', 'me', 'pickup-point', 'update'
    And header Authorization = bearer(token)
    And request { latitude: '#(lat)', longitude: '#(lng)', address: '#(address)', district: '#(district)' }
    When method put
    Then status 200
