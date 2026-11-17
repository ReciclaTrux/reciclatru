@ignore
Feature: Crea un usuario listo para usar (registro, login, perfil y ubicación)
  Argumentos (todos opcionales): role (NEIGHBOR|RECYCLER), complete (true|false), lat, lng, district
  Devuelve: id, token, email, role, fullName, phone, reference, address, district

  Scenario: Crear usuario
    * def role = karate.get('role', 'NEIGHBOR')
    * def complete = karate.get('complete', true)
    * def lat = karate.get('lat', lat)
    * def lng = karate.get('lng', lng)
    * def district = karate.get('district', 'Huanchaco')
    * def suffix = java.util.UUID.randomUUID().toString().replace('-', '').substring(0, 12)
    * def email = 'karate.' + suffix + '@example.com'
    * def phone = '9' + java.util.concurrent.ThreadLocalRandom.current().nextInt(10000000, 99999999)
    * def fullName = (role == 'NEIGHBOR' ? 'Vecino Karate ' : 'Reciclador Karate ') + suffix.substring(0, 6)
    * def reference = 'Referencia ' + suffix.substring(0, 6)
    * def address = 'Jr. Karate ' + suffix.substring(0, 6) + ' 123'

    Given url baseUrl
    And path 'party-data-management', 'initiation'
    And request { email: '#(email)', password: '#(testPassword)', role: '#(role)' }
    When method post
    Then status 201
    * def id = response.crReferenceId

    Given url baseUrl
    And path 'party-authentication', 'initiation'
    And request { email: '#(email)', password: '#(testPassword)' }
    When method post
    Then status 200
    * def token = response.accessToken

    * if (complete) karate.call('classpath:reciclatru/common/complete-profile.feature', { token: token, role: role, fullName: fullName, phone: phone, reference: reference, address: address, district: district, lat: lat, lng: lng })
