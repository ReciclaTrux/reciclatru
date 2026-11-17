Feature: Privacidad de datos personales (RN13, RNF-04)
  El backend decide qué campos envía según el rol y el estado del pedido; no basta con ocultarlos en la app.

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def chosen = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER', alias: 'elegido' }
    * def other = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER', alias: 'otro' }
    * def stranger = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER', alias: 'ajeno' }
    * def leaks = function(text){ return [neighbor.phone, neighbor.address, neighbor.reference, neighbor.fullName, neighbor.email].filter(function(v){ return text.contains(v) }) }

  Scenario: Un reciclador que no aceptó el pedido no ve nada que identifique al vecino
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(stranger.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord contains { counterpart: null, pickup: null, alreadyAccepted: false }
    * def text = karate.toString(response)
    And match leaks(text) == []

  Scenario: Tras aceptar, el reciclador ve nombre y teléfono pero no dirección ni referencia
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(chosen.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(chosen.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.counterpart == { fullName: '#(neighbor.fullName)', phone: '#(neighbor.phone)' }
    And match response.servicingOrderInstanceRecord.pickup == null
    * def text = karate.toString(response)
    And match leaks(text) contains only ['#(neighbor.fullName)', '#(neighbor.phone)']

  Scenario: Solo el reciclador elegido, con el pedido confirmado, ve la dirección exacta
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(chosen.token)', orderId: '#(created.orderId)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(other.token)', orderId: '#(created.orderId)' }
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)', recyclerName: '#(chosen.fullName)' }
    # el elegido sí
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(chosen.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.pickup contains { address: '#(neighbor.address)', reference: '#(neighbor.reference)' }
    # el descartado no
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(other.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord contains { acceptanceStatus: 'DISCARDED', pickup: null, counterpart: null }
    * def text = karate.toString(response)
    And match leaks(text) == []
    # uno que nunca aceptó, tampoco (el pedido ya no está disponible)
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(stranger.token)
    When method get
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_AVAILABLE')

  Scenario: Con el pedido recolectado, el reciclador elegido deja de ver dirección y teléfono
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(chosen.token)', orderId: '#(created.orderId)' }
    * def done = call read('classpath:reciclatru/common/confirm-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)', recyclerName: '#(chosen.fullName)' }
    * def done = call read('classpath:reciclatru/common/fulfill-order.feature') { token: '#(neighbor.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(chosen.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord contains { status: 'COLLECTED', pickup: null, counterpart: null }
    * def text = karate.toString(response)
    And match leaks(text) == []

  Scenario: La lista de pedidos disponibles nunca incluye datos del vecino
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', 'availability', 'retrieval'
    And param limit = 50
    And header Authorization = bearer(stranger.token)
    When method get
    Then status 200
    * def text = karate.toString(response)
    And match leaks(text) == []

  Scenario: Las respuestas de error no exponen datos internos
    Given path 'servicing-order', UUID_ZERO, 'retrieval'
    And header Authorization = bearer(stranger.token)
    When method get
    Then status 404
    * def text = karate.toString(response)
    And assert !text.contains('stack')
    And assert !text.contains('node_modules')
    And assert !text.toLowerCase().contains('select ')
    And assert !text.toLowerCase().contains('postgres')
