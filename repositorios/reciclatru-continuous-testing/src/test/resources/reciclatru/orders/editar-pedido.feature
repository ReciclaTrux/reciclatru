Feature: Editar un pedido (HU-07, RN11)

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def stranger = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', alias: 'stranger' }
    * def recycler = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }
    * def newItems = [{ description: 'Papel de oficina', weightKg: 3 }]

  # ───────────── Happy path ─────────────

  @smoke
  Scenario: Mientras está pendiente y sin aceptaciones, el vecino edita los ítems
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'update'
    And header Authorization = bearer(neighbor.token)
    And request { items: '#(newItems)' }
    When method put
    Then status 200
    And match response.servicingOrderInstanceRecord contains { id: '#(created.orderId)', status: 'PENDING', totalWeightKg: 3 }
    And match response.servicingOrderInstanceRecord.items == newItems
    # la consulta posterior confirma que se guardó
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.items == newItems

  # ───────────── Unhappy path ─────────────

  Scenario: Aplica las mismas validaciones que al publicar (sin ítems)
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'update'
    And header Authorization = bearer(neighbor.token)
    And request { items: [] }
    When method put
    Then status 422
    And match response contains problem(422, 'INVALID_ITEMS')

  Scenario: Aplica las mismas validaciones que al publicar (peso inválido)
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'update'
    And header Authorization = bearer(neighbor.token)
    And request { items: [{ description: 'Cartón', weightKg: 0 }] }
    When method put
    Then status 422
    And match response contains problem(422, 'INVALID_WEIGHT')

  Scenario: Con una aceptación ya no se puede editar
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'update'
    And header Authorization = bearer(neighbor.token)
    And request { items: '#(newItems)' }
    When method put
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_EDITABLE')

  Scenario: Un pedido cancelado no se puede editar
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/cancel-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'update'
    And header Authorization = bearer(neighbor.token)
    And request { items: '#(newItems)' }
    When method put
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_EDITABLE')

  Scenario: Otro vecino no puede editar un pedido ajeno
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'update'
    And header Authorization = bearer(stranger.token)
    And request { items: '#(newItems)' }
    When method put
    Then status 403
    And match response contains problem(403, 'NOT_ORDER_OWNER')
    # el pedido original sigue igual
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.totalWeightKg == 12.5

  Scenario: Un reciclador no puede editar pedidos
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'update'
    And header Authorization = bearer(recycler.token)
    And request { items: '#(newItems)' }
    When method put
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario: Editar un pedido que no existe responde 404
    Given path 'servicing-order', UUID_ZERO, 'update'
    And header Authorization = bearer(neighbor.token)
    And request { items: '#(newItems)' }
    When method put
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')
