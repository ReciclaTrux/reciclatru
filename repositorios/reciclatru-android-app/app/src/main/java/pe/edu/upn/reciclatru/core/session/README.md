# core/session

Sesión activa del usuario: token JWT, rol (Vecino/Reciclador), identificador y estado de
autenticación expuesto como `StateFlow` para las guardas de navegación.

- Guarda el token en almacenamiento seguro del dispositivo; nunca en texto plano ni en logs.
- No autoriza nada por sí misma: el rol que expone la app es solo para UX y navegación; la
  API valida el token y los permisos en cada petición.
- No contiene lógica de login: eso es de `feature/auth`.
