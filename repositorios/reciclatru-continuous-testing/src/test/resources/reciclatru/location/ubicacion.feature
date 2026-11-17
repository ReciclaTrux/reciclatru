Feature: Ubicación de recojo del vecino (HU-04)

  Background:
    * url baseUrl

  # ───────────── Happy path ─────────────

  Scenario: El vecino registra su punto de recojo y el perfil queda completo
    * def mine = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', complete: false }
    Given path 'party-data-management', 'me', 'update'
    And header Authorization = bearer(mine.token)
    And request { fullName: '#(mine.fullName)', phone: '#(mine.phone)' }
    When method put
    Then status 200
    And match response.partyDataManagementInstanceRecord.profileComplete == false
    Given path 'location-data-management', 'me', 'pickup-point', 'update'
    And header Authorization = bearer(mine.token)
    And request { latitude: -8.077806, longitude: -79.122183, address: 'Av. Víctor Larco 1280, Huanchaco', district: 'Huanchaco' }
    When method put
    Then status 200
    And match response.partyDataManagementInstanceRecord.pickupPoint == { latitude: -8.077806, longitude: -79.122183, address: 'Av. Víctor Larco 1280, Huanchaco', district: 'Huanchaco' }
    And match response.partyDataManagementInstanceRecord.profileComplete == true

  Scenario: Las coordenadas se redondean a 6 decimales
    * def mine = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    Given path 'location-data-management', 'me', 'pickup-point', 'update'
    And header Authorization = bearer(mine.token)
    And request { latitude: -8.07780649, longitude: -79.12218349, address: 'Dirección', district: 'Huanchaco' }
    When method put
    Then status 200
    And match response.partyDataManagementInstanceRecord.pickupPoint contains { latitude: -8.077806, longitude: -79.122183 }

  Scenario: Cambiar la ubicación solo afecta a los pedidos nuevos (RN18)
    * def mine = call read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', district: 'Huanchaco' }
    * def first = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    * def done = call read('classpath:reciclatru/common/set-location.feature') { token: '#(mine.token)', address: 'Jr. Nuevo 456', district: 'Trujillo Centro', lat: '#(farLat)', lng: '#(farLng)' }
    * def second = call read('classpath:reciclatru/common/create-order.feature') { token: '#(mine.token)' }
    Given path 'servicing-order', first.orderId, 'retrieval'
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.zone == 'Huanchaco'
    Given path 'servicing-order', second.orderId, 'retrieval'
    And header Authorization = bearer(mine.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.zone == 'Trujillo Centro'

  # ───────────── Unhappy path ─────────────

  Scenario: Un reciclador no puede registrar punto de recojo (su ubicación no se guarda, RN19)
    * def recycler = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }
    Given path 'location-data-management', 'me', 'pickup-point', 'update'
    And header Authorization = bearer(recycler.token)
    And request { latitude: -8.077806, longitude: -79.122183 }
    When method put
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario Outline: Rechaza coordenadas inválidas (<caso>)
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    Given path 'location-data-management', 'me', 'pickup-point', 'update'
    And header Authorization = bearer(neighbor.token)
    And request { latitude: <latitud>, longitude: <longitud> }
    When method put
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')

    Examples:
      | caso                 | latitud  | longitud   |
      | latitud mayor a 90   | 91       | -79.12     |
      | latitud menor a -90  | -91      | -79.12     |
      | longitud mayor a 180 | -8.07    | 181        |
      | longitud menor a -180| -8.07    | -181       |
      | latitud como texto   | 'abc'    | -79.12     |

  Scenario: Falta la longitud
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    Given path 'location-data-management', 'me', 'pickup-point', 'update'
    And header Authorization = bearer(neighbor.token)
    And request { latitude: -8.07 }
    When method put
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')
    And match response.errors contains { field: 'longitude', message: '#string' }
