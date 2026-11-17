# Backend
Requiere Java 21 y Maven. Ejecutar `mvn spring-boot:run`. API en http://localhost:8080.

Para habilitar Uber Direct define UBER_CLIENT_ID, UBER_CLIENT_SECRET y UBER_CUSTOMER_ID y cambia delivery.uber.enabled a true. Si no se configuran credenciales, Uber usa tarifa mock para que el checkout siga funcionando.

**Importante:** este prototipo usa memoria local y no está listo para producción. Antes de producción: persistencia, autenticación de webhooks, validación de firma, idempotencia durable, control de inventario, HTTPS, gestión segura de secretos y reconciliación de pagos.
