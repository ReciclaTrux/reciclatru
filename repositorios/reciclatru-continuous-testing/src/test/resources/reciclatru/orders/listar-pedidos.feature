Feature: Mis pedidos del vecino (HU-06)

  Background:
    * url baseUrl
    * def recycler = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }

  # ───────────── Happy path ─────────────

  Scenario: Cada vecino solo ve sus propios pedidos
    * def a = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def b = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def orderA = call read('classpath:reciclatru/common/create-order.feature') { token: '#(a.token)' }
    * def orderB = call read('classpath:reciclatru/common/create-order.feature') { token: '#(b.token)' }
    Given path 'servicing-order', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(a.token)
    When method get
    Then status 200
    And match response.items[*].id == ['#(orderA.orderId)']
    Given path 'servicing-order', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(b.token)
    When method get
    Then status 200
    And match response.items[*].id == ['#(orderB.orderId)']

  Scenario: El resumen es liviano y trae los datos para la tarjeta del pedido
    * def mine = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    Given path 'servicing-order', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.nextCursor == null
    And match response.items == '#[1]'
    And match response.items[0] contains { id: '#(created.orderId)', status: 'PENDING', itemCount: 2, totalWeightKg: 12.5, zone: 'Huanchaco', acceptanceCount: 0, createdAt: '#string' }

  Scenario: Del más reciente al más antiguo
    * def mine = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def first = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    * def second = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    Given path 'servicing-order', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.items[*].id == ['#(second.orderId)', '#(first.orderId)']

  Scenario: Un pedido cancelado pasa de ACTIVE a HISTORY
    * def mine = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    * def done = call read('classpath:reciclatru/common/cancel-order.feature') { token: '#(mine.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.items == []
    Given path 'servicing-order', 'retrieval'
    And param tab = 'HISTORY'
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.items[*].id == ['#(created.orderId)']
    And match response.items[0].status == 'CANCELLED'

  Scenario: Pedido pendiente con aceptaciones muestra cuántas lleva; confirmado, el nombre del reciclador
    * def mine = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.items[0] contains { status: 'PENDING', acceptanceCount: 1 }
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(mine.token)', orderId: '#(created.orderId)', recyclerName: '#(recycler.fullName)' }
    Given path 'servicing-order', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.items[0] contains { status: 'CONFIRMED', chosenRecyclerName: '#(recycler.fullName)' }

  Scenario: Paginación por cursor sin repetir ni perder pedidos
    * def mine = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def o1 = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    * def o2 = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    * def o3 = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    Given path 'servicing-order', 'retrieval'
    And params { tab: 'ACTIVE', limit: 2 }
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.items[*].id == ['#(o3.orderId)', '#(o2.orderId)']
    And match response.nextCursor == '#string'
    * def cursor = response.nextCursor
    Given path 'servicing-order', 'retrieval'
    And params { tab: 'ACTIVE', limit: 2, cursor: '#(cursor)' }
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.items[*].id == ['#(o1.orderId)']
    And match response.nextCursor == null

  # ───────────── Unhappy path ─────────────

  Scenario Outline: Rechaza parámetros inválidos (<caso>)
    * def mine = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    Given path 'servicing-order', 'retrieval'
    And params <params>
    And header Authorization = bearer(mine.token)
    When method get
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')

    Examples:
      | caso                  | params                       |
      | pestaña desconocida   | { tab: 'OTRA' }              |
      | límite en cero        | { limit: 0 }                 |
      | límite sobre el máximo| { limit: 51 }                |
      | límite no numérico    | { limit: 'muchos' }          |
      | cursor alterado       | { cursor: 'esto-no-es-valido' } |

  Scenario: Un reciclador no usa "Mis pedidos" del vecino
    Given path 'servicing-order', 'retrieval'
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')
