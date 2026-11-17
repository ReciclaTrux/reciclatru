@ignore
Feature: Un reciclador acepta un pedido
  Argumentos: token (del reciclador), orderId
  Devuelve: acceptedOrder

  Scenario: Aceptar
    Given url baseUrl
    And path 'servicing-order', orderId, 'acceptance', 'initiation'
    And header Authorization = bearer(token)
    When method post
    Then status 201
    * def acceptedOrder = response.servicingOrderInstanceRecord
