@ignore
Feature: Sube una foto a un pedido
  Argumentos: token, orderId, file (ruta classpath:), filename, contentType
  Devuelve: photoId

  Scenario: Subir
    Given url baseUrl
    And path 'servicing-order', orderId, 'photo', 'initiation'
    And header Authorization = bearer(token)
    And multipart file file = { read: '#(file)', filename: '#(filename)', contentType: '#(contentType)' }
    When method post
    Then status 201
    * def photoId = response.id
