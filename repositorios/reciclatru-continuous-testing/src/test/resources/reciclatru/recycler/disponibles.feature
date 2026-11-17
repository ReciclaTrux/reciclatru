Feature: Pedidos disponibles para el reciclador (HU-10)

  Background:
    * url baseUrl
    * def recycler = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def farNeighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', alias: 'far', lat: '#(farLat)', lng: '#(farLng)', district: 'Trujillo Centro' }

  # ───────────── Happy path ─────────────

  @smoke
  Scenario: Sin ubicación se ordenan por fecha y no incluyen datos personales del vecino
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', 'availability', 'retrieval'
    And param limit = 50
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    And match response.sortedBy == 'DATE'
    # Coincidencia exacta: ninguna tarjeta puede traer dirección, referencia, nombre ni teléfono.
    And match each response.items == { id: '#uuid', status: 'PENDING', itemCount: '#number', totalWeightKg: '#number', thumbnailUrl: '##string', zone: '##string', createdAt: '#string', approximateDistanceKm: '##number', alreadyAccepted: '#boolean' }
    And match response.items[*].id contains created.orderId

  Scenario: Con lat y lng se ordenan del más cercano al más lejano
    * def done = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/create-order.feature') { token: '#(farNeighbor.token)' }
    Given path 'servicing-order', 'availability', 'retrieval'
    And params { lat: '#(lat)', lng: '#(lng)', limit: 50 }
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    And match response.sortedBy == 'DISTANCE'
    * def distances = $response.items[*].approximateDistanceKm
    And assert distances.length >= 2
    And assert isAscending(distances)

  Scenario: El pedido que está en el mismo punto del reciclador sale primero, con distancia 0
    # Punto aleatorio propio: otras corridas o usuarios de la base compartida no se mezclan.
    * def jLat = -8.3 - Math.random() / 10
    * def jLng = -79.3 - Math.random() / 10
    * def isolated = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', lat: '#(jLat)', lng: '#(jLng)' }
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(isolated.token)' }
    Given path 'servicing-order', 'availability', 'retrieval'
    And params { lat: '#(jLat)', lng: '#(jLng)', limit: 1 }
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    And match response.sortedBy == 'DISTANCE'
    And match response.items[0] contains { id: '#(created.orderId)', approximateDistanceKm: 0 }

  Scenario: Solo aparecen pedidos pendientes
    * def mine = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def other = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/cancel-order.feature') { token: '#(neighbor.token)', orderId: '#(other.orderId)' }
    Given path 'servicing-order', 'availability', 'retrieval'
    And param limit = 50
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    And match each response.items contains { status: 'PENDING' }
    And match response.items[*].id contains mine.orderId
    And match response.items[*].id !contains other.orderId

  Scenario: Un pedido que el reciclador ya aceptó se marca con alreadyAccepted
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', 'availability', 'retrieval'
    And param limit = 50
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    * def item = karate.filter(response.items, function(i){ return i.id == created.orderId })[0]
    And match item.alreadyAccepted == true

  Scenario: Respeta el límite de resultados por página
    * def done = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', 'availability', 'retrieval'
    And param limit = 1
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 200
    And match response.items == '#[1]'
    And match response.nextCursor == '#string'

  # ───────────── Unhappy path ─────────────

  Scenario: Un vecino no puede ver la lista de disponibles
    Given path 'servicing-order', 'availability', 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario Outline: Rechaza parámetros inválidos (<caso>)
    Given path 'servicing-order', 'availability', 'retrieval'
    And params <params>
    And header Authorization = bearer(recycler.token)
    When method get
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')

    Examples:
      | caso                | params                         |
      | latitud sobre 90    | { lat: 200, lng: 1 }           |
      | longitud sobre 180  | { lat: 1, lng: 181 }           |
      | latitud no numérica | { lat: 'norte', lng: 1 }       |
      | límite en cero      | { limit: 0 }                   |
      | límite sobre 50     | { limit: 51 }                  |
      | cursor alterado     | { cursor: 'esto-no-es-valido' } |
