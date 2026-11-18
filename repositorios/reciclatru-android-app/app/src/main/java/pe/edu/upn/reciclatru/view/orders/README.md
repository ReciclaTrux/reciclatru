# view/orders

**Dominio:** pedidos — publicación, seguimiento propio y cancelación.

Pedidos del vecino: publicar un pedido con materiales, pesos y fotos, ver los propios y cancelarlo. El estado y las transiciones válidas los define la API.

- `view/orders/` — `CreateOrderScreen`, `MyOrdersScreen`, `OrderDetailScreen` y `OrderFormat`.
- `viewmodel/orders/` — un ViewModel y un `UiState` por pantalla.
- `domain/usecase/orders/`, `domain/repository/orders/` y `model/orders/`.
- `data/remote/*/orders/`, `data/repository/orders/` y `data/local/orders/` (compresión y almacén de fotos).
