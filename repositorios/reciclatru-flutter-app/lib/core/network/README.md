# core/network

Cliente HTTP compartido y todo lo que rodea una llamada: URL base, interceptores (token de
sesión, cabeceras, errores), timeouts y traducción de errores HTTP a un tipo común
(`ApiException`).

- No define DTOs ni repositorios de un dominio: eso vive en `features/<dominio>/data/`.
- No decide permisos: solo transporta la petición y devuelve éxito, error de red o error de
  la API con su mensaje.
