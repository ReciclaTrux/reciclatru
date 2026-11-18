# view/auth

**Dominio:** autenticación — registro e inicio de sesión.

Registro e inicio de sesión de vecinos y recicladores. El rol elegido en el registro viaja en las peticiones, pero quien autoriza es la API.

- `view/auth/` — `LoginScreen` y `RegisterScreen`; solo dibujan estado y envían eventos.
- `viewmodel/auth/` — `LoginViewModel`, `RegisterViewModel`, `LogoutViewModel` y sus estados.
- `domain/usecase/auth/`, `domain/repository/auth/` y `model/auth/` — casos de uso, contrato y modelos.
- `data/remote/*/auth/` y `data/repository/auth/` — API, DTOs, mapper e implementación.
