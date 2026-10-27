# core/session

Sesión activa del usuario: token JWT, rol (Vecino/Reciclador), identificador y estado de
autenticación expuesto como `StateFlow`.

| Archivo | Qué hace |
| --- | --- |
| `Session.kt` | `Session(userId, token, role)` y `UserRole` con el valor del `enum` de la API. |
| `SessionStore.kt` | Guarda y lee la sesión en DataStore; expone `session: StateFlow<Session?>`, `awaitSession()` para los repositorios y `currentSession` para el interceptor. |

Reglas:

- Guarda el token en almacenamiento del dispositivo; nunca en texto plano en logs. El
  logging HTTP tacha `Authorization`.
- No autoriza nada por sí misma: el rol que expone la app es solo para UX y navegación; la
  API valida el token y los permisos en cada petición.
- No contiene lógica de login: eso es de `feature/auth` (HU-01/HU-02).
