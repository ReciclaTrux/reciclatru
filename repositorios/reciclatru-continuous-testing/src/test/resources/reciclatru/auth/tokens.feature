Feature: Todas las rutas protegidas exigen un token válido (RNF-04)

  Background:
    * url baseUrl
    * def user = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }

  Scenario Outline: <metodo> <ruta> sin token responde 401
    Given url baseUrl + '<ruta>'
    When method <metodo>
    Then status 401
    And match header Content-Type contains 'application/problem+json'
    And match response contains problem(401, 'UNAUTHENTICATED')

    Examples:
      | metodo | ruta                                                              |
      | GET    | /party-data-management/me/retrieval                               |
      | PUT    | /party-data-management/me/update                                  |
      | PUT    | /location-data-management/me/pickup-point/update                  |
      | POST   | /servicing-order/initiation                                       |
      | GET    | /servicing-order/retrieval                                        |
      | GET    | /servicing-order/availability/retrieval                           |
      | GET    | /servicing-order/acceptance/retrieval                             |
      | GET    | /servicing-order/00000000-0000-4000-8000-000000000000/retrieval   |
      | PUT    | /servicing-order/00000000-0000-4000-8000-000000000000/update      |
      | PUT    | /servicing-order/00000000-0000-4000-8000-000000000000/termination |
      | POST   | /servicing-order/00000000-0000-4000-8000-000000000000/acceptance/initiation |
      | GET    | /servicing-order/00000000-0000-4000-8000-000000000000/acceptance/retrieval |
      | PUT    | /servicing-order/00000000-0000-4000-8000-000000000000/confirmation/execution |
      | PUT    | /servicing-order/00000000-0000-4000-8000-000000000000/fulfillment/execution |
      | POST   | /servicing-order/00000000-0000-4000-8000-000000000000/photo/initiation |

  Scenario Outline: Un token <caso> es rechazado
    Given path 'servicing-order', 'retrieval'
    And header Authorization = '<cabecera>'
    When method get
    Then status 401
    And match response contains problem(401, 'UNAUTHENTICATED')

    Examples:
      | caso                    | cabecera                   |
      | inventado               | Bearer abc.def.ghi         |
      | sin la palabra Bearer   | abc.def.ghi                |
      | con esquema Basic       | Basic dXNlcjpwYXNz         |
      | vacío                   | Bearer                     |

  Scenario: Un token válido al que le cambian un carácter deja de servir (firma)
    * def tampered = user.token.substring(0, user.token.length - 2) + (user.token.endsWith('AA') ? 'BB' : 'AA')
    Given path 'servicing-order', 'retrieval'
    And header Authorization = bearer(tampered)
    When method get
    Then status 401
    And match response contains problem(401, 'UNAUTHENTICATED')

  Scenario: El token de un vecino no abre rutas de reciclador
    Given path 'servicing-order', 'availability', 'retrieval'
    And header Authorization = bearer(user.token)
    When method get
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')
