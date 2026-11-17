Feature: Perfil del usuario (HU-03)

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def other = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', alias: 'other' }

  # ───────────── Happy path ─────────────

  Scenario: Un usuario recién registrado tiene el perfil incompleto
    * def fresh = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', complete: false }
    Given path 'party-data-management', 'me', 'retrieval'
    And header Authorization = bearer(fresh.token)
    When method get
    Then status 200
    And match response.partyDataManagementInstanceRecord contains { profileComplete: false, fullName: null, phone: null, pickupPoint: null }

  Scenario: El dueño ve su perfil completo, con el celular real
    Given path 'party-data-management', 'me', 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.partyDataManagementInstanceRecord contains
      """
      { id: '#(neighbor.id)', email: '#(neighbor.email)', role: 'NEIGHBOR', fullName: '#(neighbor.fullName)',
        phone: '#(neighbor.phone)', reference: '#(neighbor.reference)', profileComplete: true }
      """
    And match response.partyDataManagementInstanceRecord.pickupPoint contains { address: '#(neighbor.address)', district: 'Huanchaco' }

  Scenario: Pedir el perfil con el id propio equivale al alias me
    Given path 'party-data-management', neighbor.id, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.crReferenceId == neighbor.id

  Scenario: Se puede editar solo un campo sin tocar los demás
    * def mine = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    Given path 'party-data-management', 'me', 'update'
    And header Authorization = bearer(mine.token)
    And request { reference: 'Portón azul, frente a la bodega' }
    When method put
    Then status 200
    And match response.partyDataManagementInstanceRecord contains { reference: 'Portón azul, frente a la bodega', fullName: '#(mine.fullName)', phone: '#(mine.phone)' }

  Scenario: Un reciclador completa su perfil con nombre y celular
    * def recycler = call read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER', complete: false }
    Given path 'party-data-management', 'me', 'update'
    And header Authorization = bearer(recycler.token)
    And request { fullName: 'Jorge Luis Mendoza Ruiz', phone: '944123456' }
    When method put
    Then status 200
    And match response.partyDataManagementInstanceRecord contains { role: 'RECYCLER', fullName: 'Jorge Luis Mendoza Ruiz', phone: '944123456' }

  # ───────────── Unhappy path ─────────────

  Scenario Outline: Rechaza un celular inválido (<valor>)
    Given path 'party-data-management', 'me', 'update'
    And header Authorization = bearer(neighbor.token)
    And request { phone: '<valor>' }
    When method put
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')
    And match response.errors contains { field: 'phone', message: '#string' }

    Examples:
      | valor        |
      | 12345        |
      | 887654321    |
      | 98765432     |
      | 9876543210   |
      | abcdefghi    |

  Scenario Outline: Rechaza un nombre inválido (<caso>)
    * def name = '<caso>' == 'muy largo' ? new Array(82).join('a') : 'A'
    Given path 'party-data-management', 'me', 'update'
    And header Authorization = bearer(neighbor.token)
    And request { fullName: '#(name)' }
    When method put
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')
    And match response.errors contains { field: 'fullName', message: '#string' }

    Examples:
      | caso      |
      | muy corto |
      | muy largo |

  Scenario: La referencia no puede superar 150 caracteres
    * def long = new Array(152).join('a')
    Given path 'party-data-management', 'me', 'update'
    And header Authorization = bearer(neighbor.token)
    And request { reference: '#(long)' }
    When method put
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')
    And match response.errors contains { field: 'reference', message: '#string' }

  Scenario: Un cuerpo vacío no actualiza nada
    Given path 'party-data-management', 'me', 'update'
    And header Authorization = bearer(neighbor.token)
    And request {}
    When method put
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')

  Scenario: El rol y el correo no se pueden cambiar (RN02, RN03)
    Given path 'party-data-management', 'me', 'update'
    And header Authorization = bearer(neighbor.token)
    And request { role: 'RECYCLER', email: 'otro@example.com' }
    When method put
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')
    And match response.errors contains { field: 'role', message: '#string' }
    And match response.errors contains { field: 'email', message: '#string' }

  Scenario: Nadie puede ver el perfil de otro usuario
    Given path 'party-data-management', other.id, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')

  Scenario: Nadie puede editar el perfil de otro usuario
    Given path 'party-data-management', other.id, 'update'
    And header Authorization = bearer(neighbor.token)
    And request { fullName: 'Nombre Impostor' }
    When method put
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')
    # y el perfil ajeno quedó intacto
    Given path 'party-data-management', 'me', 'retrieval'
    And header Authorization = bearer(other.token)
    When method get
    Then status 200
    And match response.partyDataManagementInstanceRecord.fullName == other.fullName
