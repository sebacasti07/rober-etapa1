# Rober Motos — prototipo delivery

## Requisitos
- Java 21 + Maven
- Node.js 20+ y npm

## Ejecutar backend
```bash
cd backend
mvn spring-boot:run
```

## Ejecutar frontend
```bash
cd frontend
npm install
npm run dev
```
Abrir http://localhost:5173.

## Desplegar backend en Render
1. Crea un servicio **Web Service** y selecciona **Docker** como runtime.
2. Deja **Root Directory** vacío para usar la raíz del repositorio, donde están `Dockerfile` y `.dockerignore`.
3. Render construye la imagen y asigna `PORT`; el contenedor configura Spring Boot para escuchar en ese puerto.
4. Si habilitas Uber Direct, agrega `UBER_CLIENT_ID`, `UBER_CLIENT_SECRET` y `UBER_CUSTOMER_ID` como variables de entorno.

El backend de este prototipo mantiene órdenes y cotizaciones en memoria, así que los reinicios o despliegues en Render eliminan esos datos. Webpay todavía usa credenciales de integración del SDK y una URL de retorno local codificada en el servicio; no habilites pagos reales sin adaptar y verificar esa configuración.

## Configuración
Por defecto Uber también usa mock si no se habilita o no tiene credenciales. Para intentar Uber Direct sandbox: exportar `UBER_CLIENT_ID`, `UBER_CLIENT_SECRET`, `UBER_CUSTOMER_ID` y cambiar `delivery.uber.enabled: true` en `backend/src/main/resources/application.yml`. Nunca subir secretos a Git.

Transbank utiliza credenciales de integración del SDK. Para el retorno local, la aplicación debe ser accesible desde el navegador de pruebas; en ciertos escenarios se requiere túnel HTTPS.

## Limitaciones deliberadas del prototipo
- Estado, órdenes y cotizaciones se guardan en memoria; reiniciar backend borra datos.
- El failover está simplificado y `simulate-no-riders` aplica una simulación general, no una respuesta real del courier.
- Webhooks de demostración no validan firma ni autenticidad; no exponerlos públicamente.
- El adaptador Uber contiene una implementación inicial ilustrativa y debe ajustarse al esquema regional/actual de la cuenta.
- La respuesta de retorno Webpay requiere revisión de parámetros de cancelación y validación de monto/orden en persistencia antes de producción.
- El seguimiento mock avanza cada cinco segundos; los webhooks pueden sobrescribir estado.
- Direcciones y coordenadas son puntos de referencia de demostración, no geocodificación de direcciones exactas.
