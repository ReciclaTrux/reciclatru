# core/router

Rutas con go_router: definición de rutas, navegación por nombre y guardas (`redirect`) según
sesión y rol. Define **dónde** se puede ir (login, pedidos, explorar, seguimiento).

- Las pantallas no viven acá: cada una está en `features/<dominio>/presentation/`.
- Las guardas solo redirigen por sesión o rol para mejorar la UX; el backend sigue autorizando.
- Al agregar una pantalla, se registra su ruta y su `builder`.
