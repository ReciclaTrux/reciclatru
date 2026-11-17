@ignore
Feature: El vecino lista las aceptaciones de su pedido
  Argumentos: token, orderId
  Devuelve: items

  Scenario: Listar
    Given url baseUrl
    And path 'servicing-order', orderId, 'acceptance', 'retrieval'
    And header Authorization = bearer(token)
    When method get
    Then status 200
    * def items = response.items
