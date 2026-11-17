@ignore
Feature: El vecino cancela un pedido
  Argumentos: token, orderId

  Scenario: Cancelar
    Given url baseUrl
    And path 'servicing-order', orderId, 'termination'
    And header Authorization = bearer(token)
    When method put
    Then status 200
    * def cancelledOrder = response.servicingOrderInstanceRecord
