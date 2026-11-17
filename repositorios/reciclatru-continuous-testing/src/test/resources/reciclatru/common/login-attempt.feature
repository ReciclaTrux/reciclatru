@ignore
Feature: Un intento de login con contraseña incorrecta
  Argumentos: email
  Devuelve: statusCode, retryAfter, body

  Scenario: Intento
    Given url baseUrl
    And path 'party-authentication', 'initiation'
    And request { email: '#(email)', password: 'clave-incorrecta-1' }
    When method post
    * def statusCode = responseStatus
    * def retryAfter = karate.get('responseHeaders["Retry-After"][0]', null)
    * def body = response
