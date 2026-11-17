// Cabeceras comunes a todas las peticiones. Un feature puede fijar su propio identificador con
// `* def correlationId = 'mi-id'` antes de la petición (útil para probar la trazabilidad).
function fn() {
  var custom = karate.get('correlationId');
  return { 'x-correlation-id': custom ? custom : 'karate-' + java.util.UUID.randomUUID() };
}
