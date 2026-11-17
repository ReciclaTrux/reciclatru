Feature: Cancelar un pedido (HU-08, RN16, RN17)

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def stranger = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', alias: 'stranger' }
    * def recycler = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }

  # ───────────── Happy path ─────────────

  @smoke
  Scenario: El vecino cancela un pedido pendiente (borrado lógico)
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'termination'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 200
    And match response.servicingOrderInstanceRecord contains { id: '#(created.orderId)', status: 'CANCELLED', availableActions: [] }
    # el pedido NO se borra: sigue consultable
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.status == 'CANCELLED'

  Scenario: También se puede cancelar un pedido ya confirmado
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)', recyclerName: '#(recycler.fullName)' }
    Given path 'servicing-order', created.orderId, 'termination'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 200
    And match response.servicingOrderInstanceRecord.status == 'CANCELLED'

  Scenario: El reciclador que había aceptado lo ve cancelado y deja de ver dirección y teléfono
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    * def done = call read('classpath:reciclatru/common/cancel-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', 'acceptance', 'retrieval'
    And param tab = 'HISTORY'
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    * def mine = karate.filter(response.items, function(i){ return i.orderId == created.orderId })
    And match mine[0].acceptanceLabel == 'CANCELLED_BY_NEIGHBOR'
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord contains { status: 'CANCELLED', counterpart: null, pickup: null }

  Scenario: Un pedido cancelado deja de aparecer entre los disponibles del reciclador
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/cancel-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', 'availability', 'retrieval'
    And params { limit: 50 }
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    And match response.items[*].id !contains created.orderId

  # ───────────── Unhappy path ─────────────

  Scenario: No se puede cancelar dos veces
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/cancel-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'termination'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_CANCELLABLE')

  Scenario: No se puede cancelar un pedido ya recolectado
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)', recyclerName: '#(recycler.fullName)' }
    * def done = call read('classpath:reciclatru/common/fulfill-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'termination'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_CANCELLABLE')

  Scenario: Otro vecino no puede cancelar un pedido ajeno
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'termination'
    And header Authorization = bearer(stranger.token)
    When method put
    Then status 403
    And match response contains problem(403, 'NOT_ORDER_OWNER')
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.status == 'PENDING'

  Scenario: Un reciclador no puede cancelar pedidos
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'termination'
    And header Authorization = bearer(recycler.token)
    When method put
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario: Cancelar un pedido que no existe responde 404
    Given path 'servicing-order', UUID_ZERO, 'termination'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')
