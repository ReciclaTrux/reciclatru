@ignore
Feature: Completa el perfil (y la ubicación si es vecino)
  Argumentos: token, role, fullName, phone, reference, address, district, lat, lng

  Scenario: Perfil y ubicación
    Given url baseUrl
    And path 'party-data-management', 'me', 'update'
    And header Authorization = bearer(token)
    # Dentro de una expresión JS no se evalúan los '#(...)': se usan las variables directamente.
    * def body = role == 'NEIGHBOR' ? { fullName: fullName, phone: phone, reference: reference } : { fullName: fullName, phone: phone }
    And request body
    When method put
    Then status 200

    * def completeLocation = role == 'NEIGHBOR'
    * if (completeLocation) karate.call('classpath:reciclatru/common/set-location.feature', { token: token, address: address, district: district, lat: lat, lng: lng })
