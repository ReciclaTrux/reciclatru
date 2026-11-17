Feature: Elegir reciclador y marcar como recolectado (HU-13, HU-16)

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def stranger = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', alias: 'stranger' }
    * def recycler = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }
    # Pedido con una aceptación, listo para confirmar. Se crea uno nuevo por escenario con la función `fresh`.
    * def fresh = function(){ var o = karate.call('classpath:reciclatru/common/create-order.feature', { token: neighbor.token }); karate.call('classpath:reciclatru/common/accept-order.feature', { token: recycler.token, orderId: o.orderId }); return o.orderId }
    * def acceptanceOf = function(orderId){ var r = karate.call('classpath:reciclatru/common/list-acceptances.feature', { token: neighbor.token, orderId: orderId }); return r.items[0].id }

  # ───────────── Happy path ─────────────

  Scenario: Elegir reciclador confirma el pedido y marca su aceptación como elegida
    * def orderId = fresh()
    * def acceptanceId = acceptanceOf(orderId)
    Given path 'servicing-order', orderId, 'confirmation', 'execution'
    And header Authorization = bearer(neighbor.token)
    And request { acceptanceId: '#(acceptanceId)' }
    When method put
    Then status 200
    And match response.servicingOrderInstanceRecord contains { status: 'CONFIRMED', chosenRecyclerName: '#(recycler.fullName)', counterpart: { fullName: '#(recycler.fullName)', phone: '#(recycler.phone)' } }
    Given path 'servicing-order', orderId, 'acceptance', 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.items[0].status == 'CHOSEN'

  # ───────────── Unhappy path: elegir ─────────────

  Scenario: No se puede elegir dos veces (doble toque)
    * def orderId = fresh()
    * def acceptanceId = acceptanceOf(orderId)
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(orderId)', recyclerName: '#(recycler.fullName)' }
    Given path 'servicing-order', orderId, 'confirmation', 'execution'
    And header Authorization = bearer(neighbor.token)
    And request { acceptanceId: '#(acceptanceId)' }
    When method put
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_PENDING')

  Scenario: No se puede elegir en un pedido cancelado
    * def orderId = fresh()
    * def acceptanceId = acceptanceOf(orderId)
    * def done = call read('classpath:reciclatru/common/cancel-order.feature') { token: '#(neighbor.token)', orderId: '#(orderId)' }
    Given path 'servicing-order', orderId, 'confirmation', 'execution'
    And header Authorization = bearer(neighbor.token)
    And request { acceptanceId: '#(acceptanceId)' }
    When method put
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_PENDING')

  Scenario: El identificador de aceptación debe ser un UUID
    * def orderId = fresh()
    Given path 'servicing-order', orderId, 'confirmation', 'execution'
    And header Authorization = bearer(neighbor.token)
    And request { acceptanceId: 'abc' }
    When method put
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')
    And match response.errors contains { field: 'acceptanceId', message: '#string' }

  Scenario: Falta el identificador de aceptación
    * def orderId = fresh()
    Given path 'servicing-order', orderId, 'confirmation', 'execution'
    And header Authorization = bearer(neighbor.token)
    And request {}
    When method put
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')

  Scenario: Una aceptación que no existe responde 404
    * def orderId = fresh()
    Given path 'servicing-order', orderId, 'confirmation', 'execution'
    And header Authorization = bearer(neighbor.token)
    And request { acceptanceId: '#(UUID_ZERO)' }
    When method put
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')

  Scenario: No se puede usar la aceptación de otro pedido
    * def orderA = fresh()
    * def orderB = fresh()
    * def acceptanceOfB = acceptanceOf(orderB)
    Given path 'servicing-order', orderA, 'confirmation', 'execution'
    And header Authorization = bearer(neighbor.token)
    And request { acceptanceId: '#(acceptanceOfB)' }
    When method put
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')
    # el pedido A sigue pendiente
    Given path 'servicing-order', orderA, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.status == 'PENDING'

  Scenario: Un reciclador no puede elegirse a sí mismo
    * def orderId = fresh()
    * def acceptanceId = acceptanceOf(orderId)
    Given path 'servicing-order', orderId, 'confirmation', 'execution'
    And header Authorization = bearer(recycler.token)
    And request { acceptanceId: '#(acceptanceId)' }
    When method put
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario: Otro vecino no puede elegir en un pedido ajeno
    * def orderId = fresh()
    * def acceptanceId = acceptanceOf(orderId)
    Given path 'servicing-order', orderId, 'confirmation', 'execution'
    And header Authorization = bearer(stranger.token)
    And request { acceptanceId: '#(acceptanceId)' }
    When method put
    Then status 403
    And match response contains problem(403, 'NOT_ORDER_OWNER')

  # ───────────── Unhappy path: recolectar ─────────────

  Scenario: No se puede marcar como recolectado un pedido sin confirmar
    * def orderId = fresh()
    Given path 'servicing-order', orderId, 'fulfillment', 'execution'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_CONFIRMED')

  Scenario: Recolectar dos veces solo marca una vez (estado final)
    * def orderId = fresh()
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(orderId)', recyclerName: '#(recycler.fullName)' }
    * def done = call read('classpath:reciclatru/common/fulfill-order.feature') { token: '#(neighbor.token)', orderId: '#(orderId)' }
    Given path 'servicing-order', orderId, 'fulfillment', 'execution'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_CONFIRMED')

  Scenario: No se puede recolectar un pedido cancelado
    * def orderId = fresh()
    * def done = call read('classpath:reciclatru/common/cancel-order.feature') { token: '#(neighbor.token)', orderId: '#(orderId)' }
    Given path 'servicing-order', orderId, 'fulfillment', 'execution'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_CONFIRMED')

  Scenario: Solo el vecino marca el pedido como recolectado, no el reciclador
    * def orderId = fresh()
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(orderId)', recyclerName: '#(recycler.fullName)' }
    Given path 'servicing-order', orderId, 'fulfillment', 'execution'
    And header Authorization = bearer(recycler.token)
    When method put
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario: Otro vecino no puede marcar como recolectado un pedido ajeno
    * def orderId = fresh()
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(orderId)', recyclerName: '#(recycler.fullName)' }
    Given path 'servicing-order', orderId, 'fulfillment', 'execution'
    And header Authorization = bearer(stranger.token)
    When method put
    Then status 403
    And match response contains problem(403, 'NOT_ORDER_OWNER')

  Scenario: Recolectar un pedido que no existe responde 404
    Given path 'servicing-order', UUID_ZERO, 'fulfillment', 'execution'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')
