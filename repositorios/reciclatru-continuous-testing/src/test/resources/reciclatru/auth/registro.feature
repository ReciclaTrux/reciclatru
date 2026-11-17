Feature: Registro de cuenta (HU-01)

  Background:
    * url baseUrl
    * def uniqueEmail = function(){ return 'karate.' + java.util.UUID.randomUUID().toString().replace('-', '').substring(0, 12) + '@example.com' }

  # ───────────── Happy path ─────────────

  @smoke
  Scenario Outline: Registro exitoso de un <role>
    * def email = uniqueEmail()
    Given path 'party-data-management', 'initiation'
    And request { email: '#(email)', password: '#(testPassword)', role: '<role>' }
    When method post
    Then status 201
    And match response.crReferenceId == '#uuid'
    # Coincidencia exacta: si la API empezara a devolver el hash de la contraseña, esta prueba falla.
    And match response.partyDataManagementInstanceRecord == { id: '#(response.crReferenceId)', email: '#(email)', role: '<role>', fullName: null, phone: null, reference: null, pickupPoint: null, profileComplete: false, createdAt: '#string' }

    Examples:
      | role      |
      | NEIGHBOR  |
      | RECYCLER  |

  Scenario: El correo se guarda en minúsculas y sin espacios
    * def email = uniqueEmail()
    * def messy = '  ' + email.toUpperCase() + '  '
    Given path 'party-data-management', 'initiation'
    And request { email: '#(messy)', password: '#(testPassword)', role: 'NEIGHBOR' }
    When method post
    Then status 201
    And match response.partyDataManagementInstanceRecord.email == email

  # ───────────── Unhappy path ─────────────

  Scenario: No permite registrar dos veces el mismo correo
    * def email = uniqueEmail()
    Given path 'party-data-management', 'initiation'
    And request { email: '#(email)', password: '#(testPassword)', role: 'NEIGHBOR' }
    When method post
    Then status 201
    Given path 'party-data-management', 'initiation'
    And request { email: '#(email)', password: '#(testPassword)', role: 'RECYCLER' }
    When method post
    Then status 409
    And match header Content-Type contains 'application/problem+json'
    And match response contains problem(409, 'EMAIL_ALREADY_REGISTERED')

  Scenario: El correo repetido se detecta aunque cambien mayúsculas o espacios
    * def email = uniqueEmail()
    Given path 'party-data-management', 'initiation'
    And request { email: '#(email)', password: '#(testPassword)', role: 'NEIGHBOR' }
    When method post
    Then status 201
    * def variant = ' ' + email.toUpperCase()
    Given path 'party-data-management', 'initiation'
    And request { email: '#(variant)', password: '#(testPassword)', role: 'NEIGHBOR' }
    When method post
    Then status 409
    And match response contains problem(409, 'EMAIL_ALREADY_REGISTERED')

  Scenario Outline: Rechaza datos inválidos: <caso>
    Given path 'party-data-management', 'initiation'
    And request { email: '<email>', password: '<password>', role: '<role>' }
    When method post
    Then status 400
    And match header Content-Type contains 'application/problem+json'
    And match response contains problem(400, 'VALIDATION_ERROR')
    And match response.errors contains { field: '<campo>', message: '#string' }

    Examples:
      | caso                    | email              | password       | role      | campo    |
      | contraseña de 7 letras  | ok@example.com     | Corta12        | NEIGHBOR  | password |
      | correo sin arroba       | sin-arroba         | Reciclatru2026 | NEIGHBOR  | email    |
      | correo incompleto       | rosa@              | Reciclatru2026 | NEIGHBOR  | email    |
      | rol inexistente         | ok2@example.com    | Reciclatru2026 | ADMIN     | role     |
      | rol en minúsculas       | ok3@example.com    | Reciclatru2026 | neighbor  | role     |

  Scenario Outline: Rechaza un cuerpo sin el campo <falta>
    Given path 'party-data-management', 'initiation'
    And request <cuerpo>
    When method post
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')
    And match response.errors contains { field: '<falta>', message: '#string' }

    Examples:
      | falta    | cuerpo                                                       |
      | email    | { password: 'Reciclatru2026', role: 'NEIGHBOR' }             |
      | password | { email: 'falta.pass@example.com', role: 'NEIGHBOR' }        |
      | role     | { email: 'falta.rol@example.com', password: 'Reciclatru2026' } |

  Scenario: Rechaza campos que no existen en el contrato (evita asignación masiva)
    * def email = uniqueEmail()
    Given path 'party-data-management', 'initiation'
    And request { email: '#(email)', password: '#(testPassword)', role: 'NEIGHBOR', isAdmin: true }
    When method post
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')
    And match response.errors contains { field: 'isAdmin', message: '#string' }

  Scenario: Rechaza un cuerpo vacío
    Given path 'party-data-management', 'initiation'
    And request {}
    When method post
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')

  Scenario: La respuesta de error no revela la contraseña enviada
    * def email = uniqueEmail()
    Given path 'party-data-management', 'initiation'
    And request { email: '#(email)', password: 'x', role: 'NEIGHBOR' }
    When method post
    Then status 400
    * def text = karate.toString(response)
    And assert !text.contains('"password":"x"')
