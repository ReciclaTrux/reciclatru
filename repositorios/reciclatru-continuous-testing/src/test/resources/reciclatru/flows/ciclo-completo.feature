Feature: Ciclo completo de un pedido, de la publicación a la recolección (happy path de punta a punta)

  Background:
    * url baseUrl

  @smoke
  Scenario: Vecino publica → dos recicladores aceptan → vecino elige a uno → el pedido se recolecta
    * def neighbor = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def jorge = call read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }
    * def maria = call read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }

    # 1. El vecino publica el pedido
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def orderId = created.orderId

    # 2. Ambos recicladores lo ven disponible y lo aceptan
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(jorge.token)', orderId: '#(orderId)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(maria.token)', orderId: '#(orderId)' }

    # 3. El vecino ve las dos aceptaciones
    Given url baseUrl
    And path 'servicing-order', orderId, 'acceptance', 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.items[*].fullName == ['#(jorge.fullName)', '#(maria.fullName)']

    # 4. El vecino elige a Jorge: el pedido queda CONFIRMED
    * def confirmation = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(orderId)', recyclerName: '#(jorge.fullName)' }
    And match confirmation.confirmedOrder contains { status: 'CONFIRMED', chosenRecyclerName: '#(jorge.fullName)', availableActions: ['MARK_COLLECTED', 'CANCEL'] }

    # 5. Jorge (elegido) ahora ve la dirección exacta para llegar (RN13)
    Given url baseUrl
    And path 'servicing-order', orderId, 'retrieval'
    And header Authorization = bearer(jorge.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord contains { status: 'CONFIRMED', acceptanceStatus: 'CHOSEN' }
    And match response.servicingOrderInstanceRecord.pickup contains { address: '#(neighbor.address)', reference: '#(neighbor.reference)', latitude: '#number', longitude: '#number' }
    And match response.servicingOrderInstanceRecord.counterpart == { fullName: '#(neighbor.fullName)', phone: '#(neighbor.phone)' }

    # 6. María (no elegida) queda descartada y no ve dirección ni teléfono
    Given url baseUrl
    And path 'servicing-order', orderId, 'retrieval'
    And header Authorization = bearer(maria.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord contains { acceptanceStatus: 'DISCARDED', pickup: null, counterpart: null }

    # 7. En "Mis pedidos aceptados", Jorge lo ve elegido y María no elegido
    Given url baseUrl
    And path 'servicing-order', 'acceptance', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(jorge.token)
    When method get
    Then status 200
    * def mine = karate.filter(response.items, function(i){ return i.orderId == orderId })
    And match mine[0].acceptanceLabel == 'CHOSEN'
    Given url baseUrl
    And path 'servicing-order', 'acceptance', 'retrieval'
    And param tab = 'HISTORY'
    And header Authorization = bearer(maria.token)
    When method get
    Then status 200
    * def hers = karate.filter(response.items, function(i){ return i.orderId == orderId })
    And match hers[0].acceptanceLabel == 'NOT_CHOSEN'

    # 8. El vecino marca el pedido como recolectado (estado final)
    * def collection = call read('classpath:reciclatru/common/fulfill-order.feature') { token: '#(neighbor.token)', orderId: '#(orderId)' }
    And match collection.collectedOrder contains { status: 'COLLECTED', availableActions: [] }

    # 9. Jorge deja de ver dirección y teléfono
    Given url baseUrl
    And path 'servicing-order', orderId, 'retrieval'
    And header Authorization = bearer(jorge.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord contains { status: 'COLLECTED', pickup: null, counterpart: null }

    # 10. El pedido pasa al historial de ambos
    Given url baseUrl
    And path 'servicing-order', 'retrieval'
    And param tab = 'HISTORY'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.items[*].id contains orderId
    And match response.items[0].status == 'COLLECTED'
