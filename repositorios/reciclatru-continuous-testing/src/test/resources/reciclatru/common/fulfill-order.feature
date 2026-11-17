@ignore
Feature: El vecino marca el pedido como recolectado
  Argumentos: token, orderId

  Scenario: Recolectar
    Given url baseUrl
    And path 'servicing-order', orderId, 'fulfillment', 'execution'
    And header Authorization = bearer(token)
    When method put
    Then status 200
    * def collectedOrder = response.servicingOrderInstanceRecord
