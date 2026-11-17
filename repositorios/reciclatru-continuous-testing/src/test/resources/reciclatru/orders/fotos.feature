Feature: Fotos del pedido (HU-09, RN07)
  Los escenarios marcados @storage necesitan el bucket de fotos real (o un S3 local). Sin él, se omiten con -Dstorage=false.

  Background:
    * url baseUrl
    * def neighbor = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }
    * def stranger = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR', alias: 'stranger' }
    * def recycler = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'RECYCLER' }
    * def upload =
      """
      function(token, orderId, file, filename, contentType) {
        return karate.call('classpath:reciclatru/common/upload-photo.feature', { token: token, orderId: orderId, file: file, filename: filename, contentType: contentType });
      }
      """

  # ───────────── Happy path ─────────────

  @storage @smoke
  Scenario: El vecino sube una foto PNG y aparece en el pedido
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'photo', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And multipart file file = { read: 'classpath:fixtures/pixel.png', filename: 'foto.png', contentType: 'image/png' }
    When method post
    Then status 201
    And match response.id == '#uuid'
    # la url es la ruta estable del endpoint de recuperación, nunca un enlace permanente al bucket
    And match response.url == '/api/v1/servicing-order/' + created.orderId + '/photo/' + response.id + '/retrieval'
    * def photoId = response.id
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.photos[*].id contains photoId

  @storage
  Scenario: La foto se descarga mediante una redirección a una URL firmada de vida corta
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def uploaded = upload(neighbor.token, created.orderId, 'classpath:fixtures/pixel.png', 'foto.png', 'image/png')
    * configure followRedirects = false
    Given path 'servicing-order', created.orderId, 'photo', uploaded.photoId, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 302
    And match header Location == '#regex .*X-Amz-Signature=.*'
    And match header Cache-Control contains 'private'

  @storage
  Scenario: El vecino quita una foto mientras el pedido es editable
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def uploaded = upload(neighbor.token, created.orderId, 'classpath:fixtures/pixel.png', 'foto.png', 'image/png')
    Given path 'servicing-order', created.orderId, 'photo', uploaded.photoId, 'termination'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 204
    Given path 'servicing-order', created.orderId, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 200
    And match response.servicingOrderInstanceRecord.photos == []

  @storage
  Scenario: Quitar la misma foto dos veces responde 404
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def uploaded = upload(neighbor.token, created.orderId, 'classpath:fixtures/pixel.png', 'foto.png', 'image/png')
    Given path 'servicing-order', created.orderId, 'photo', uploaded.photoId, 'termination'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 204
    Given path 'servicing-order', created.orderId, 'photo', uploaded.photoId, 'termination'
    And header Authorization = bearer(neighbor.token)
    When method put
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')

  # ───────────── Unhappy path ─────────────

  @storage
  Scenario: Máximo 3 fotos por pedido
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def p1 = upload(neighbor.token, created.orderId, 'classpath:fixtures/pixel.png', 'foto.png', 'image/png')
    * def p2 = upload(neighbor.token, created.orderId, 'classpath:fixtures/pixel.png', 'foto.png', 'image/png')
    * def p3 = upload(neighbor.token, created.orderId, 'classpath:fixtures/pixel.png', 'foto.png', 'image/png')
    Given path 'servicing-order', created.orderId, 'photo', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And multipart file file = { read: 'classpath:fixtures/pixel.png', filename: 'foto.png', contentType: 'image/png' }
    When method post
    Then status 422
    And match response contains problem(422, 'PHOTO_LIMIT_REACHED')

  @storage
  Scenario: Rechaza una foto de más de 2 MB
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    # Un PNG válido rellenado con ceros hasta ~2,2 MB
    * def big = java.util.Arrays.copyOf(karate.readAsBytes('classpath:fixtures/pixel.png'), 2300000)
    Given path 'servicing-order', created.orderId, 'photo', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And multipart file file = { value: '#(big)', filename: 'grande.png', contentType: 'image/png' }
    When method post
    Then status 413
    And match response contains problem(413, 'PHOTO_TOO_LARGE')

  Scenario: Rechaza un archivo que no es JPEG ni PNG, aunque diga serlo
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'photo', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And multipart file file = { read: 'classpath:fixtures/nota.txt', filename: 'foto.png', contentType: 'image/png' }
    When method post
    Then status 422
    And match response contains problem(422, 'PHOTO_INVALID_TYPE')

  Scenario: Rechaza la petición si falta el archivo
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'photo', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And multipart field nombre = 'sin archivo'
    When method post
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')

  Scenario: Un reciclador no puede subir fotos
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'photo', 'initiation'
    And header Authorization = bearer(recycler.token)
    And multipart file file = { read: 'classpath:fixtures/pixel.png', filename: 'foto.png', contentType: 'image/png' }
    When method post
    Then status 403
    And match response contains problem(403, 'FORBIDDEN_ROLE')

  Scenario: Otro vecino no puede subir fotos a un pedido ajeno
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'photo', 'initiation'
    And header Authorization = bearer(stranger.token)
    And multipart file file = { read: 'classpath:fixtures/pixel.png', filename: 'foto.png', contentType: 'image/png' }
    When method post
    Then status 403
    And match response contains problem(403, 'NOT_ORDER_OWNER')

  Scenario: Subir foto a un pedido que no existe responde 404
    Given path 'servicing-order', UUID_ZERO, 'photo', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And multipart file file = { read: 'classpath:fixtures/pixel.png', filename: 'foto.png', contentType: 'image/png' }
    When method post
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')

  Scenario: Con una aceptación el pedido ya no admite fotos nuevas
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    * def done = call read('classpath:reciclatru/common/accept-order.feature') { token: '#(recycler.token)', orderId: '#(created.orderId)' }
    Given path 'servicing-order', created.orderId, 'photo', 'initiation'
    And header Authorization = bearer(neighbor.token)
    And multipart file file = { read: 'classpath:fixtures/pixel.png', filename: 'foto.png', contentType: 'image/png' }
    When method post
    Then status 409
    And match response contains problem(409, 'ORDER_NOT_EDITABLE')

  Scenario: Pedir una foto que no existe responde 404
    * def created = call read('classpath:reciclatru/common/create-order.feature') { token: '#(neighbor.token)' }
    Given path 'servicing-order', created.orderId, 'photo', UUID_ZERO, 'retrieval'
    And header Authorization = bearer(neighbor.token)
    When method get
    Then status 404
    And match response contains problem(404, 'NOT_FOUND')
