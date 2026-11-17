@ratelimit
Feature: Límite de solicitudes (429)
  Requiere el API con RATE_LIMIT_ENABLED=true (límites por defecto: 10 login/registro y 100 generales por minuto).
  Se ejecuta aparte: mvn test -Dsuite=ratelimit   (con RATE_LIMIT_ENABLED=false casi todo el resto de la suite se rompería).

  Background:
    * url baseUrl
    * def loginAttempt =
      """
      function(email) {
        var r = karate.call('classpath:reciclatru/common/login-attempt.feature', { email: email });
        return r;
      }
      """

  Scenario: Demasiados logins fallidos seguidos reciben 429 con Retry-After
    * def email = 'karate.rate.' + java.util.UUID.randomUUID().toString().substring(0, 8) + '@example.com'
    * def results = karate.repeat(15, function(i){ return loginAttempt(email) })
    * def statuses = karate.map(results, function(r){ return r.statusCode })
    # los primeros intentos son credenciales incorrectas normales...
    And match statuses[0] == 401
    # ...y después aparece el bloqueo
    And match statuses contains 429
    * def blocked = karate.filter(results, function(r){ return r.statusCode == 429 })[0]
    And match blocked.retryAfter == '#regex \\d+'
    And match blocked.body contains { status: 429, code: 'TOO_MANY_REQUESTS', detail: '#string' }

  Scenario: El registro también tiene un límite estricto
    * def results = karate.repeat(15, function(i){ return karate.call('classpath:reciclatru/common/register-attempt.feature', { n: i }).statusCode })
    And match results contains 429

  Scenario: Superar el límite general por minuto también responde 429
    * def results = karate.repeat(110, function(i){ return karate.call('classpath:reciclatru/common/anonymous-get.feature', {}).statusCode })
    And match results contains 429
    # las que pasaron, pasaron como 401 (sin token), nunca como un error de servidor
    * def allowed = karate.filter(results, function(s){ return s != 429 })
    And match each allowed == 401

  Scenario: /health nunca se limita (lo usan los monitores)
    * def results = karate.repeat(130, function(i){ return karate.call('classpath:reciclatru/common/health-check.feature', {}).statusCode })
    And match each results == 200
