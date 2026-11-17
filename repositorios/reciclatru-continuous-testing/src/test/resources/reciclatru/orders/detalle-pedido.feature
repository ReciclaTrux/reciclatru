Feature: Detalle de un pedido (HU-06, HU-11)

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def stranger = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', alias: 'stranger' }
    * def order = callonce read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }

  # ───────────── Happy path ─────────────

  Scenario: El vecino dueño ve el detalle de su pedido
    Given path 'servicing-order', order.orderId, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.crReferenceId == order.orderId
    And match response.servicingOrderInstanceRecord contains { id: '#(order.orderId)', status: 'PENDING', totalWeightKg: 12.5, zone: 'Huanchaco', photos: [], availableActions: ['EDIT', 'CANCEL'] }
    And match response.servicingOrderInstanceRecord.items == '#[2]'

  # ───────────── Unhappy path ─────────────

  Scenario: Otro vecino no puede ver un pedido ajeno
    Given path 'servicing-order', order.orderId, 'retrieval'
    And header Authorization = bearer(stranger.token)
    When method get
    Then status 403
    And match response contains problem(403, 'NOT_ORDER_OWNER')

  Scenario: Un pedido que no existe responde 404
    Given path 'servicing-order', UUID_ZERO, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')

  Scenario: Un identificador mal formado responde 404 y no un error interno
    Given path 'servicing-order', 'no-es-un-uuid', 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')
