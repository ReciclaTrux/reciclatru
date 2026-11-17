@ignore
Feature: El vecino elige a un reciclador que ya aceptó
  Argumentos: token (del vecino), orderId, recyclerName (nombre del reciclador elegido)
  Devuelve: acceptanceId, confirmedOrder

  Scenario: Confirmar
    Given url baseUrl
    And path 'servicing-order', orderId, 'acceptance', 'retrieval'
    And header Authorization = bearer(token)
    When method get
    Then status 200
    * def picked = karate.filter(response.items, function(a){ return a.fullName == recyclerName })
    * def acceptanceId = picked[0].id

    Given url baseUrl
    And path 'servicing-order', orderId, 'confirmation', 'execution'
    And header Authorization = bearer(token)
    And request { acceptanceId: '#(acceptanceId)' }
    When method put
    Then status 200
    * def confirmedOrder = response.servicingOrderInstanceRecord
