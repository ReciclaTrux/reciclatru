Feature: Publicar un pedido (HU-05)

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def recycler = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }
    * def oneItem = { description: 'Botellas PET', weightKg: 1 }

  # ───────────── Happy path ─────────────

  @smoke
  Scenario: El vecino publica un pedido con dos ítems
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And request { items: [{ description: 'Botellas de plástico PET', weightKg: 4.5 }, { description: 'Cartón corrugado', weightKg: 8.0 }] }
    When method post
    Then status 201
    And match response.crReferenceId == '#uuid'
    And match response.servicingOrderInstanceRecord contains
      """
      {
        id: '#(response.crReferenceId)', status: 'PENDING', totalWeightKg: 12.5, photos: [], zone: 'Huanchaco',
        acceptanceCount: 0, chosenRecyclerName: null, counterpart: null, availableActions: ['EDIT', 'CANCEL'], createdAt: '#string'
      }
      """
    And match response.servicingOrderInstanceRecord.items == [{ description: 'Botellas de plástico PET', weightKg: 4.5 }, { description: 'Cartón corrugado', weightKg: 8 }]

  # Hallazgo: el contrato OpenAPI promete el encabezado Location en el 201, pero la API aún no lo envía.
  # Está excluido de la ejecución normal (@known-issue). Quita la etiqueta cuando la API lo implemente.
  @known-issue
  Scenario: La respuesta 201 incluye el encabezado Location apuntando al pedido
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And request { items: ['#(oneItem)'] }
    When method post
    Then status 201
    And match header Location == '#string'
    And match header Location contains response.crReferenceId

  Scenario: El pedido recién creado aparece en "Mis pedidos" activos
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', 'retrieval'
    And param tab = 'ACTIVE'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.items[*].id contains created.orderId

  Scenario: Un pedido puede llevar hasta 10 ítems
    * def items = karate.repeat(10, function(i){ return { description: 'Ítem ' + (i + 1), weightKg: 1 } })
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And request { items: '#(items)' }
    When method post
    Then status 201
    And match response.servicingOrderInstanceRecord.totalWeightKg == 10
    And match response.servicingOrderInstanceRecord.items == '#[10]'

  Scenario Outline: Acepta pesos en el borde válido (<peso> kg)
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And request { items: [{ description: 'Chatarra', weightKg: <peso> }] }
    When method post
    Then status 201
    And match response.servicingOrderInstanceRecord.totalWeightKg == <peso>

    Examples:
      | peso |
      | 0.1  |
      | 500  |

  # ───────────── Unhappy path ─────────────

  Scenario Outline: Rechaza una cantidad de ítems inválida (<caso>)
    * def items = karate.repeat(<cantidad>, function(i){ return { description: 'Ítem', weightKg: 1 } })
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And request { items: '#(items)' }
    When method post
    Then status 422
    And match header Content-Type contains 'application/problem+json'
    And match response contains problem(422, 'INVALID_ITEMS')

    Examples:
      | caso        | cantidad |
      | ninguno     | 0        |
      | once ítems  | 11       |

  Scenario Outline: Rechaza un peso inválido (<peso> kg)
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And request { items: [{ description: 'Chatarra', weightKg: <peso> }] }
    When method post
    Then status 422
    And match response contains problem(422, 'INVALID_WEIGHT')
    And match response.errors contains { field: 'items[0].weightKg', message: '#string' }

    Examples:
      | peso   |
      | 0      |
      | -2     |
      | 500.1  |
      | 100000 |

  Scenario Outline: Rechaza un ítem mal formado (<caso>)
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And request <cuerpo>
    When method post
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')

    Examples:
      | caso                    | cuerpo                                                  |
      | sin la lista de ítems   | {}                                                      |
      | ítems no es una lista   | { items: 'botellas' }                                   |
      | descripción vacía       | { items: [{ description: '', weightKg: 1 }] }           |
      | sin descripción         | { items: [{ weightKg: 1 }] }                            |
      | sin peso                | { items: [{ description: 'Cartón' }] }                  |
      | peso como texto         | { items: [{ description: 'Cartón', weightKg: 'mucho' }] } |

  Scenario: Un vecino sin ubicación no puede publicar (RN20)
    * def noLocation = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', complete: false }
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(noLocation.token)
    And request { items: ['#(oneItem)'] }
    When method post
    Then status 422
    And match response contains problem(422, 'PROFILE_INCOMPLETE')

  Scenario: Un reciclador no puede publicar pedidos
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(recycler.token)
    And request { items: ['#(oneItem)'] }
    When method post
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario: Rechaza campos que no existen en el contrato
    Given path 'servicing-order', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And request { items: ['#(oneItem)'], status: 'CONFIRMED' }
    When method post
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')
