# viewmodel/auth

`LoginViewModel`, `RegisterViewModel` y `LogoutViewModel` con sus `LoginUiState` y
`RegisterUiState`. Cada ViewModel expone un `StateFlow` inmutable, recibe eventos y llama
casos de uso de `domain/usecase/auth`. Nunca conoce Compose.
