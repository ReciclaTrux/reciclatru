Feature: Cabeceras de seguridad, trazabilidad y manejo de errores del API

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }

  # ───────────── Cabeceras de seguridad (helmet) ─────────────

  Scenario Outline: Las respuestas llevan la cabecera <cabecera>
    Given path 'health'
    When method get
    Then status 200
    And assert hasHeader(responseHeaders, '<cabecera>')

    Examples:
      | cabecera                   |
      | X-Content-Type-Options     |
      | X-Frame-Options            |
      | Strict-Transport-Security  |
      | Referrer-Policy            |
      | Content-Security-Policy    |

  Scenario: X-Content-Type-Options evita que el navegador adivine el tipo de contenido
    Given path 'health'
    When method get
    Then status 200
    And match header X-Content-Type-Options == 'nosniff'

  Scenario: No se anuncia la tecnología del servidor
    Given path 'health'
    When method get
    Then status 200
    And assert !hasHeader(responseHeaders, 'X-Powered-By')

  Scenario: Las respuestas de error también llevan cabeceras de seguridad
    Given path 'servicing-order', 'retrieval'
    When method get
    Then status 401
    And assert hasHeader(responseHeaders, 'X-Content-Type-Options')
    And assert !hasHeader(responseHeaders, 'X-Powered-By')

  # ───────────── Trazabilidad (x-correlation-id) ─────────────

  Scenario: Si el cliente envía un x-correlation-id válido, la API lo conserva
    * def correlationId = 'karate-trazabilidad-001'
    Given path 'health'
    When method get
    Then status 200
    And match header x-correlation-id == 'karate-trazabilidad-001'

  Scenario: Un x-correlation-id inválido se reemplaza por un UUID generado por el servidor
    * def correlationId = 'id con espacios y símbolos!!'
    Given path 'health'
    When method get
    Then status 200
    And match header x-correlation-id == '#uuid'

  Scenario: Un x-correlation-id de más de 100 caracteres se reemplaza
    * def correlationId = new Array(102).join('a')
    Given path 'health'
    When method get
    Then status 200
    And match header x-correlation-id == '#uuid'

  Scenario: Los errores repiten el identificador en el cuerpo, para poder rastrearlos en los logs
    * def correlationId = 'karate-error-trace-77'
    Given path 'servicing-order', UUID_ZERO, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 404
    And match header x-correlation-id == 'karate-error-trace-77'
    And match response.correlationId == 'karate-error-trace-77'

  # ───────────── Errores genéricos (RFC 7807) ─────────────

  Scenario: Una ruta que no existe responde 404 en formato problem+json
    Given path 'esta-ruta-no-existe'
    When method get
    Then status 404
    And match header Content-Type contains 'application/problem+json'
    And match response contains problem(404, 'NOT_FOUND')

  Scenario: Un JSON mal formado responde 400, no 500
    Given path 'party-authentication', 'initiation'
    And header Content-Type = 'application/json'
    And request '{"email": '
    When method post
    Then status 400
    And match header Content-Type contains 'application/problem+json'
    And match response contains { status: 400, code: 'VALIDATION_ERROR', type: '#string', title: '#string', instance: '#string' }

  Scenario: Un cuerpo JSON mayor a 50 KB se rechaza con 413
    * def big = new Array(60001).join('a')
    Given path 'party-authentication', 'initiation'
    And request { email: '#(big)', password: 'Reciclatru2026' }
    When method post
    Then status 413
    And match header Content-Type contains 'application/problem+json'
    And match response contains { status: 413, code: 'PAYLOAD_TOO_LARGE' }

  # Hallazgo: el contrato dice que TODA respuesta lleva x-correlation-id, pero los errores que rechaza el lector de JSON
  # (cuerpo mal formado, cuerpo demasiado grande) salen antes del middleware y no lo traen. Excluidos con @known-issue.
  @known-issue
  Scenario Outline: Incluso los errores del lector de JSON llevan x-correlation-id (<caso>)
    * def cuerpo = '<caso>' == 'JSON mal formado' ? '{"email": ' : '{"email": "' + new Array(60001).join('a') + '"}'
    Given path 'party-authentication', 'initiation'
    And header Content-Type = 'application/json'
    And request cuerpo
    When method post
    Then status <estado>
    And assert hasHeader(responseHeaders, 'x-correlation-id')
    And match response.correlationId == '#string'

    Examples:
      | caso            | estado |
      | JSON mal formado | 400   |
      | cuerpo enorme   | 413    |

  Scenario: Los errores no exponen trazas, rutas internas ni detalles de la base de datos
    Given path 'servicing-order', 'no-es-un-uuid', 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 404
    * def text = karate.toString(response).toLowerCase()
    And assert !text.contains('stack')
    And assert !text.contains('node_modules')
    And assert !text.contains('/home/')
    And assert !text.contains('postgres')
    And assert !text.contains('select ')

  # ───────────── CORS ─────────────

  @cors
  Scenario: Un origen permitido recibe las cabeceras CORS en el preflight
    Given path 'health'
    And header Origin = corsAllowedOrigin
    And header Access-Control-Request-Method = 'GET'
    When method options
    Then status 204
    And match header Access-Control-Allow-Origin == corsAllowedOrigin
    And match header Access-Control-Allow-Headers contains 'Authorization'

  @cors
  Scenario: Un origen no permitido no recibe cabeceras CORS (el navegador lo bloquea)
    Given path 'health'
    And header Origin = 'https://sitio-malicioso.example.com'
    And header Access-Control-Request-Method = 'GET'
    When method options
    And assert !hasHeader(responseHeaders, 'Access-Control-Allow-Origin')

  @cors
  Scenario: Una petición normal desde un origen no permitido tampoco recibe Access-Control-Allow-Origin
    Given path 'health'
    And header Origin = 'https://sitio-malicioso.example.com'
    When method get
    Then status 200
    And assert !hasHeader(responseHeaders, 'Access-Control-Allow-Origin')
