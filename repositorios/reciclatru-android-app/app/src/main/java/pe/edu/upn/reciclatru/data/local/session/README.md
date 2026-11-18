# data/local/session

Persistencia de la sesión activa: token JWT, rol (Vecino/Reciclador) e identificador.
Los modelos `Session`, `UserRole` y `SignOutReason` están en `model/session/`.

| Archivo | Qué hace |
| --- | --- |
| `SessionStore.kt` | Guarda y lee la sesión en DataStore; expone `session: StateFlow<Session?>`, `awaitSession()` para los repositorios, `currentSession` para el interceptor y `signedOut` con el motivo del cierre. |

Reglas:

- Guarda el token en almacenamiento del dispositivo; nunca en texto plano en logs. El
  logging HTTP tacha `Authorization`.
- No autoriza nada por sí misma: el rol que expone la app es solo para UX y navegación; la
  API valida el token y los permisos en cada petición.
- No contiene lógica de login: eso es de `domain/usecase/auth`.
