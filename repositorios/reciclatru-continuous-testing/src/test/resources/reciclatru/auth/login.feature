Feature: Inicio de sesión (HU-02)

  Background:
    * url baseUrl
    * def user = callonce read('classpath:reciclatru/common/create-user.feature') { role: 'NEIGHBOR' }

  # ───────────── Happy path ─────────────

  @smoke
  Scenario: Credenciales correctas devuelven un JWT de 30 días y el perfil
    Given path 'party-authentication', 'initiation'
    And request { email: '#(user.email)', password: '#(testPassword)' }
    When method post
    Then status 200
    And match response.crReferenceId == user.id
    And match response.tokenType == 'Bearer'
    And match response.accessToken == '#regex ^[\\w-]+\\.[\\w-]+\\.[\\w-]+$'
    And match response.partyDataManagementInstanceRecord contains { id: '#(user.id)', email: '#(user.email)', role: 'NEIGHBOR', profileComplete: true }
    * def days = java.time.Duration.between(java.time.Instant.now(), java.time.Instant.parse(response.expiresAt)).toDays()
    And assert days >= 29 && days <= 30

  Scenario: El token sirve para llamar a un endpoint protegido
    Given path 'party-data-management', 'me', 'retrieval'
    And header Authorization = bearer(user.token)
    When method get
    Then status 200
    And match response.crReferenceId == user.id

  Scenario: El correo no distingue mayúsculas
    * def upper = user.email.toUpperCase()
    Given path 'party-authentication', 'initiation'
    And request { email: '#(upper)', password: '#(testPassword)' }
    When method post
    Then status 200

  # ───────────── Unhappy path ─────────────

  Scenario: Contraseña incorrecta
    Given path 'party-authentication', 'initiation'
    And request { email: '#(user.email)', password: 'otra-clave-123' }
    When method post
    Then status 401
    And match header Content-Type contains 'application/problem+json'
    And match response contains problem(401, 'UNAUTHENTICATED')
    And match response.detail == 'Correo o contraseña incorrectos.'

  Scenario: Correo que no existe responde igual que una contraseña mala (no revela qué cuentas existen)
    Given path 'party-authentication', 'initiation'
    And request { email: 'nadie.karate@example.com', password: 'otra-clave-123' }
    When method post
    Then status 401
    And match response contains problem(401, 'UNAUTHENTICATED')
    And match response.detail == 'Correo o contraseña incorrectos.'

  Scenario Outline: Rechaza un cuerpo incompleto o inválido: <caso>
    Given path 'party-authentication', 'initiation'
    And request <cuerpo>
    When method post
    Then status 400
    And match response contains problem(400, 'VALIDATION_ERROR')

    Examples:
      | caso                | cuerpo                                       |
      | sin contraseña      | { email: 'a@example.com' }                   |
      | sin correo          | { password: 'Reciclatru2026' }               |
      | correo mal formado  | { email: 'no-es-correo', password: 'abc12345' } |
      | cuerpo vacío        | {}                                           |
