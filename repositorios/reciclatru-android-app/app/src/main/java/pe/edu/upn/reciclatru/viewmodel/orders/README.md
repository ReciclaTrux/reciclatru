# viewmodel/orders

`CreateOrderViewModel`, `MyOrdersViewModel` y `OrderDetailViewModel`, cada uno con su
`UiState`. Cubren los estados cargando, vacío y error, la validación de ítems al publicar y
la subida de fotos. `OrderDetailViewModel` también carga las aceptaciones de un pedido pendiente y
gestiona la elección del reciclador (confirmación, bloqueo de doble toque y `ORDER_NOT_PENDING`).
