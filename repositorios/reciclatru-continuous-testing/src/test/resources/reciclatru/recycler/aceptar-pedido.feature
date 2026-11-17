Feature: Aceptar un pedido (HU-12, HU-13, HU-14)

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def recycler = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }
    * def recycler2 = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER', alias: 'segundo' }

  # ───────────── Happy path ─────────────

  @smoke
  Scenario: El reciclador acepta un pedido y ve nombre y teléfono del vecino, pero no la dirección
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'acceptance', 'initiation'
    And header Authorization = bearer(recycler.token)
    When method post
    Then status 201
    And match response.servicingOrderInstanceRecord contains
      """
      {
        id: '#(created.orderId)', status: 'PENDING', alreadyAccepted: true, acceptanceStatus: 'ACCEPTED', acceptanceCount: 1,
        counterpart: { fullName: '#(neighbor.fullName)', phone: '#(neighbor.phone)' }, pickup: null
      }
      """
    * def text = karate.toString(response)
    And assert !text.contains(neighbor.address)
    And assert !text.contains(neighbor.reference)

  Scenario: La aceptación aparece en "Mis pedidos aceptados" como pendiente de confirmación
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', 'acceptance', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    * def mine = karate.filter(response.items, function(i){ return i.orderId == created.orderId })
    And match mine == '#[1]'
    And match mine[0] contains { acceptanceLabel: 'AWAITING_CONFIRMATION', zone: 'Huanchaco', totalWeightKg: 12.5, acceptedAt: '#string' }

  Scenario: El vecino ve cuántos aceptaron y quiénes (nombre y celular), del primero al último
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler2.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.acceptanceCount == 2
    Given path 'servicing-order', created.orderId, 'acceptance', 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.items == '#[2]'
    And match response.items[0] contains { status: 'ACCEPTED', fullName: '#(recycler.fullName)', phone: '#(recycler.phone)', acceptedAt: '#string' }
    And match response.items[1] contains { status: 'ACCEPTED', fullName: '#(recycler2.fullName)', phone: '#(recycler2.phone)' }

  Scenario: Aceptar dos veces el mismo pedido registra una sola aceptación (doble toque)
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'acceptance', 'initiation'
    And header Authorization = bearer(recycler.token)
    When method post
    Then status 200
    And match response.servicingOrderInstanceRecord.acceptanceCount == 1
    Given path 'servicing-order', created.orderId, 'acceptance', 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.items == '#[1]'

  # ───────────── Unhappy path ─────────────

  Scenario: Un vecino no puede aceptar pedidos
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'acceptance', 'initiation'
    And header Authorization = bearer(neighbor.token)
    When method post
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario: Aceptar un pedido que no existe responde 404
    Given path 'servicing-order', UUID_ZERO, 'acceptance', 'initiation'
    And header Authorization = bearer(recycler.token)
    When method post
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')

  Scenario: No se puede aceptar un pedido cancelado
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/cancel-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'acceptance', 'initiation'
    And header Authorization = bearer(recycler.token)
    When method post
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_AVAILABLE')

  Scenario: No se puede aceptar un pedido que otro reciclador ya tiene confirmado
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)', recyclerName: '#(recycler.fullName)' }
    Given path 'servicing-order', created.orderId, 'acceptance', 'initiation'
    And header Authorization = bearer(recycler2.token)
    When method post
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_AVAILABLE')

  Scenario: Un reciclador con el perfil incompleto no puede aceptar
    * def incomplete = call read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER', complete: false }
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'acceptance', 'initiation'
    And header Authorization = bearer(incomplete.token)
    When method post
    Then status 422
    And match response contains problem(422, 'PROFILE_INCOMPLETE')

  Scenario: Un reciclador no puede ver las aceptaciones de un pedido (solo el vecino dueño)
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'acceptance', 'retrieval'
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario: Otro vecino no puede ver las aceptaciones de un pedido ajeno
    * def stranger = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'acceptance', 'retrieval'
    And header Authorization = bearer(stranger.token)
    When method get
    Then status 403
    And match response contains problem(403, 'NOT_ORDER_OWNER')

  Scenario: Un vecino no usa "Mis pedidos aceptados" del reciclador
    Given path 'servicing-order', 'acceptance', 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')
