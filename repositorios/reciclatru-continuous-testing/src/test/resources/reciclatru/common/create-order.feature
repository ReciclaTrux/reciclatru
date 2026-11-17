@ignore
Feature: Publica un pedido
  Argumentos: token (del vecino); items (opcional)
  Devuelve: orderId, order (el registro del pedido)

  Scenario: Crear pedido
    * def items = karate.get('items', [{ description: 'Botellas de plástico PET', weightKg: 4.5 }, { description: 'Cartón corrugado', weightKg: 8 }])
    Given url baseUrl
    And path 'servicing-order', 'initiation'
    And header Authorization = bearer(token)
    And request { items: '#(items)' }
    When method post
    Then status 201
    * def orderId = response.crReferenceId
    * def order = response.servicingOrderInstanceRecord
