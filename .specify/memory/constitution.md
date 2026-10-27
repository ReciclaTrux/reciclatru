<!--
SYNC IMPACT REPORT (temporal; eliminar antes de hacer commit)
- Cambio de versión: 1.0.0 → 1.1.1 (MINOR 1.1.0 al ampliar el Principio V; PATCH 1.1.1 al
  precisar que los componentes se implementan por sprint y el uso de moléculas/organismos en pantallas).
- Principios modificados: V (mismo título, ahora NO NEGOCIABLE). Se quita el nivel
  "plantillas"; el sistema de diseño publicado es la única fuente; catálogo cerrado de 33
  componentes Rt*; orden de construcción; rutas de carpetas. Los demás principios sin cambios.
- Secciones añadidas / eliminadas: ninguna.
- TODOs diferidos: ninguno. El resolver de plantillas no se ejecutó (política de ejecución de
  PowerShell); la enmienda se aplicó sobre el documento vigente.
-->
# ReciclaTru Constitution

ReciclaTru conecta a vecinos de Trujillo (Perú) con recicladores. Es desarrollada por un equipo
universitario de 7 personas; el Release 1 se entrega el 26 de noviembre de 2026.

## Core Principles

### I. Backend como única fuente de verdad (NO NEGOCIABLE)
Toda regla de negocio, permiso por rol y regla de privacidad MUST validarse en la API NestJS.
Los clientes (Android y Flutter) MUST NOT decidir qué datos puede ver un usuario: la API responde
con DTOs distintos según el rol y el estado. Ninguna regla MUST vivir solo en un cliente; un
cliente puede replicar una validación para mejorar la experiencia, pero nunca sustituye la de la API.

### II. Un solo backend, dos clientes
Android (Kotlin, principal y primero) y Flutter (iOS y multiplataforma, después) MUST consumir la
misma API REST documentada en Swagger. No se crean endpoints exclusivos de un cliente salvo
justificación documentada en el plan de la HU.

### III. Historia de usuario como corte vertical
Cada HU MUST entregarse de punta a punta (pantalla + API + base de datos) y ser demostrable al
usuario. No existen HU "solo de backend" ni "solo de UI". Cada HU se trabaja en su propia rama y
se integra con un único Pull Request.

### IV. Arquitectura por feature y MVVM
Ambos clientes MUST organizarse por dominio (auth, profile, orders, explore, tracking) con capas
ui/presentation, domain y data. Android usa MVVM con Hilt; Flutter usa Riverpod. El backend tiene
un módulo NestJS por dominio. La misma feature MUST tener el mismo nombre en los tres proyectos.

### V. Atomic Design en ambos clientes (NO NEGOCIABLE)
El sistema de diseño de ReciclaTru, publicado en
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2, es la ÚNICA fuente de estilos y componentes.
Ya está definido: tema (tokens), átomos, moléculas y organismos, con 33 componentes de prefijo
"Rt" (9 átomos, 13 moléculas, 11 organismos). Android y Flutter MUST usar los mismos nombres y
parámetros. No existe nivel de plantillas: las pantallas viven en cada feature.
- Catálogo cerrado: MUST NOT crearse componentes nuevos, variantes locales ni equivalentes fuera
  del sistema, tampoco dentro de una feature. Si una HU necesita algo que el sistema no tiene,
  MUST proponerse primero un cambio al sistema y, una vez aprobado, implementarse en ambos
  clientes con el mismo nombre. Nunca se resuelve con un widget suelto en la pantalla.
- Orden de construcción: tema (colores, tipografía, espaciado, formas) → átomos → moléculas →
  organismos. Antes de cada sprint se implementan los componentes que sus HU requieren (más
  sus dependencias internas); las HU solo los usan. Cada componente MUST respetar su guía, su
  nombre "Rt…" y los parámetros de "Qué recibe", y MUST tener `@Preview` (Compose) o ejemplo
  (Flutter) con los mismos estados que su vista previa en el sistema.
- Uso en pantallas: si el catálogo tiene una molécula u organismo para un caso, MUST usarse;
  no se rearma con átomos. Los átomos se usan directo solo cuando ningún componente mayor
  cubre el caso.
- Rutas: Android `ui/designsystem/{theme,atoms,molecules,organisms}`; Flutter
  `lib/core/design_system/{theme,atoms,molecules,organisms}`.
- Todo componente MUST ser stateless (datos y callbacks; nunca ViewModels, providers,
  repositorios ni modelos de dominio). Solo las pantallas de cada feature conocen estado y
  navegación.
- Los átomos envuelven Material 3, no lo reemplazan.
- Colores, tipografía, espaciado, formas y tamaños MUST salir del tema; no se usan valores
  literales.

### VI. Privacidad por diseño (NO NEGOCIABLE)
Los datos personales (teléfono, dirección, coordenadas exactas) MUST exponerse solo a quien la
regla de negocio lo permite y en el estado que corresponde. La ubicación del reciclador MUST NOT
almacenarse. Las contraseñas MUST guardarse con bcrypt, la autenticación MUST usar JWT y los
secretos MUST vivir solo en variables de entorno (nunca en el repositorio).

### VII. Pruebas con propósito
- Toda regla de negocio del backend MUST tener test unitario; los flujos principales MUST tener
  test e2e de API.
- En los clientes: tests unitarios de ViewModels/providers y tests de UI con repositorios falsos
  inyectados (Hilt / overrides de Riverpod).
- Los componentes clave MUST usar testTag (Android) o Key (Flutter) para pruebas estables.
- Un PR MUST NOT integrarse si la CI falla.

### VIII. Simplicidad y alcance
Solo se construye lo que está en el release actual. Se prohíben servicios de pago y dependencias
que requieran tarjeta o API key (no Firebase, no Google Maps API). Se MUST preferir la solución
más simple que cumpla los criterios de aceptación; toda complejidad extra MUST justificarse en el
plan.

### IX. Base de datos versionada
El esquema de PostgreSQL MUST cambiar solo mediante migraciones de TypeORM incluidas en el PR de
la HU. Los registros de negocio MUST NOT borrarse físicamente cuando la regla indica cancelación
(se marca el estado correspondiente).

### X. Flujo de trabajo y trazabilidad
- Cada HU de Jira tiene su propia rama, nombrada con la clave de Jira:
  `feature/SCRUM-XX-descripcion-corta` (ej. `feature/SCRUM-12-registro`). La rama es por HU, no
  por persona.
- La clave SCRUM-XX MUST aparecer también en el título del PR y en los commits, para que Jira
  los vincule automáticamente (integración GitHub for Jira).
- Nadie sube cambios directamente a `main`: está protegida y todo entra por Pull Request.
- Cada PR MUST ser revisado y aprobado por un responsable técnico del equipo antes de
  integrarse, y solo si la CI pasa.
- Los commits MUST ser convencionales (feat, fix, chore, docs, test).
- Las herramientas de IA y Spec Kit son opcionales para implementar; lo obligatorio para todo el
  equipo es el flujo de Git anterior (desde terminal o GitHub Desktop).
- La documentación de arquitectura (C4 en Mermaid) y CLAUDE.md MUST actualizarse cuando una
  decisión los afecta.

## Stack y Restricciones Técnicas

- Backend: NestJS + TypeORM + PostgreSQL, API REST con Swagger, autenticación JWT.
- Android: Kotlin, Jetpack Compose, MVVM con Hilt (cliente principal, se entrega primero).
- Flutter: Riverpod y Widgetbook (iOS y multiplataforma, se entrega después).
- Prohibido: Firebase, Google Maps API y cualquier servicio que requiera pago, tarjeta o API key
  (Principio VIII).
- Idioma de la documentación y de los artefactos de Spec Kit: español.

## Flujo de Desarrollo y Puertas de Calidad

- Una HU solo se considera terminada cuando es demostrable de punta a punta y su PR fue
  aprobado con la CI en verde (Principios III, VII y X).
- Cada PR MUST incluir las migraciones de TypeORM, los tests y la actualización de Swagger que
  la HU requiera.
- La revisión del PR MUST verificar el cumplimiento de los Principios I a X.
- El alcance del Release 1 (26 de noviembre de 2026) se limita a lo definido en sus HU; lo demás
  se difiere a releases posteriores.

## Governance

La constitución prevalece sobre las preferencias individuales y sobre cualquier otra práctica del
equipo. Cualquier cambio se propone por Pull Request, se aprueba en equipo y actualiza la versión
semántica: MAJOR para eliminar o redefinir principios de forma incompatible, MINOR para añadir un
principio o ampliar materialmente su guía, PATCH para aclaraciones y correcciones de redacción.
Cada plan de implementación MUST pasar la verificación de estos principios o documentar la
excepción y su justificación. La revisión de cada PR comprueba el cumplimiento. La guía operativa
para el trabajo diario con IA se mantiene en CLAUDE.md.

**Version**: 1.1.1 | **Ratified**: 2026-10-26 | **Last Amended**: 2026-10-27
