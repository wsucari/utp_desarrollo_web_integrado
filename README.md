# Sistema Web Integrado para la Gestión Académica de Inudi Perú

Versión del Avance 3 con frontend Angular 22, API REST en Spring Boot 3.5.16 y Java 21. Angular conserva las rutas, pantallas, modales y operaciones de la etapa JSP y utiliza los mismos servicios académicos y la misma base de datos. La autenticación utiliza sesiones, Spring Security y CSRF; no se agregó JWT.

La interfaz Angular es la entrada predeterminada. Las JSP se conservan para comprobar regresiones del Avance 2; `FRONTEND_ENABLED=false` permite ejecutarlas. El backend mantiene su organización monolítica en capas y el empaquetado WAR.

**Estado de fidelidad:** la aplicación funciona localmente, pero no se declara finalizada toda la réplica. El alcance vigente y las correcciones están en [PARIDAD_FUNCIONAL.md](docs/PARIDAD_FUNCIONAL.md). Perfiles y reseñas se completarán después. Pasarelas, Google Drive y correo están excluidos por el usuario, no pendientes. Las pruebas satisfactorias comprueban el comportamiento desarrollado, no la equivalencia exhaustiva de cada pantalla.

Tailwind CSS 3.4.19 está integrado y compilado localmente. Acceso, navegación compartida, catálogo, ajustes responsivos y modales utilizan la hoja compilada; algunas vistas conservan estilos anteriores compatibles. Se utilizan los logotipos gráficos originales encontrados en los recursos proporcionados. No se declara una conversión exhaustiva de todas las clases ni una equivalencia visual total.

## Ejecutar en Windows

Requisitos de compilación: JDK 21, Maven 3.6.3 o posterior y Node.js 22.22.3+ o 24.15+ con npm, compatibles con Angular 22. Desde esta carpeta:

```powershell
./run.ps1
```

Para utilizar el WAR ya compilado:

```powershell
./run.ps1 -SkipBuild
```

La aplicación se abre en [http://127.0.0.1:8080](http://127.0.0.1:8080). El servidor permanece activo mientras la terminal siga ejecutándose. Ctrl+C lo detiene. El script no modifica permanentemente JAVA_HOME ni PATH. Si un servidor ya ocupa el puerto, no inicia otro proceso.

Ejecución manual, con JAVA_HOME apuntando a JDK 21 y su carpeta bin en PATH:

```powershell
npm ci
npm --prefix frontend ci
npm run build:ui
mvn -B package
java -jar target/inudi-academico.war
```

`run.ps1` prepara las dependencias de estilos y Angular si no están instaladas, compila ambas interfaces y ejecuta Maven con sus pruebas antes de iniciar el servidor. El WAR ya construido no necesita Node.js para ejecutarse. Para editar estilos en desarrollo, utiliza `npm run watch:css`. El CSS generado se conserva en `src/main/resources/static/assets/tailwind.css` y se sirve desde la propia aplicación, sin CDN. La configuración detecta componentes Angular, JSP, fragmentos y etiquetas; utiliza el prefijo `tw-` y desactiva Preflight para evitar colisiones durante la adaptación progresiva. [Documentación oficial de Tailwind CLI](https://v3.tailwindcss.com/docs/installation).

El artefacto es un WAR ejecutable con Tomcat y soporte JSP, no un JAR.

## Accesos locales de demostración

Los nombres, cursos, matrículas y precios iniciales son ficticios. Se crean únicamente cuando la base está vacía y DEMO_ENABLED=true.

| Perfil | Correo | Contraseña |
|---|---|---|
| Administración | admin@inudi.local | Inudi2026!Demo |
| Docente | docente@inudi.local | Inudi2026!Demo |
| Estudiante | estudiante@inudi.local | Inudi2026!Demo |
| Segundo estudiante | camila@inudi.local | Inudi2026!Demo |

Los certificados generados con demostración habilitada incorporan la leyenda de demostración local y no acreditan certificaciones institucionales.

## Funcionalidades

- Inicio de sesión, sesiones, contraseñas BCrypt, CSRF, roles y control de pertenencia al curso.
- Registro público de estudiantes y administración de usuarios activos/inactivos, docentes y perfiles.
- Catálogo con búsqueda y filtros; gestión de cursos, publicación y asignación de varios docentes.
- Módulos ordenados y contenidos de texto, video, material, actividad, foro y evaluación. YouTube mediante iframe y otros recursos HTTPS mediante enlace.
- Carrito de selección, solicitud de matrícula y registro/edición de pagos manuales desde un modal; confirmación transaccional del acceso y actualización de vigencia.
- Vigencia y cancelación de matrículas sin borrar el historial.
- Progreso independiente de entrega, participación o aprobación; registro idempotente, siguiente contenido y cálculo por contenidos reales.
- Entregas privadas de actividades, máximo de intentos, notas de 0 a 20 y retroalimentación docente.
- Evaluaciones activas/inactivas con 1–20 intentos o intentos ilimitados: opción múltiple, verdadero/falso, relaciones, completar texto y selección de términos. Resultados ponderados, umbral de 0–100 %, estrellas e historial.
- Publicaciones, respuestas, edición propia, retiro lógico, reacciones y valoración de aportes; reseñas de cursos.
- Certificado único por participante/curso, PDF con QR y consulta por código de siete dígitos con sesión de estudiante.
- Seguimiento por curso, exportación CSV para hojas de cálculo y registro de operaciones relevantes.
- Formularios modales de cursos, usuarios/profesores, módulos, contenidos, preguntas, pagos, calificaciones, entregas y aportes; errores y datos conservados sin abandonar la pantalla.

## Recorrido para la demostración

1. Ingresa como estudiante y abre **Cursos Inscritos**. El curso de metodología tiene una bienvenida ya completada.
2. Revisa el contenido; marca como completados el texto, video y material.
3. Envía un archivo en la actividad. Participa en el foro y aprueba la evaluación.
4. Ingresa como docente. Desde **Mis cursos**, abre el seguimiento del curso, descarga la entrega y califica en su modal.
5. Como estudiante, completa las lecciones restantes y abre **Certificados** para emitir y descargar el documento.
6. Utiliza el código o QR para consultar el certificado con sesión de estudiante.
7. Como administrador, abre **Pagos → Registrar pago**. El modal permite registrar un pago confirmado y su vigencia para habilitar matrícula. **Editar** permite actualizar importe, referencia y vigencia; estudiante/curso y el estado de un pago ya confirmado se conservan.

No existen cobros bancarios, Google Drive ni envío de correos: son exclusiones confirmadas, no integraciones futuras requeridas. La confirmación del pago es administrativa; registrar una solicitud pendiente no habilita por sí solo el acceso.

## Base de datos y archivos

H2 persiste en `data/inudi.mv.db`; los archivos privados se guardan en `uploads/`. Estas carpetas y `target/` están excluidas de Git. Reiniciar la aplicación conserva los registros y archivos, pero cierra las sesiones.

Flyway aplica V1 (esquema), V2 (restricciones), V3 (sesiones), V4 (vigencia del pago) y V5 (reglas de evaluación) de `src/main/resources/db/migration/`. Hibernate valida el esquema; no lo recrea ni elimina datos al iniciar. Son 19 tablas académicas/de negocio y `user_sessions`; Flyway mantiene además su tabla técnica de historial.

El material y las entregas se descargan mediante controladores con autorización, no mediante una carpeta pública. Se admiten PDF, DOCX, XLSX, PPTX, TXT, PNG, JPG y ZIP de hasta 10 MB, con comprobación básica de extensión y firma de los formatos binarios. No se incluye un motor antivirus.

Para una copia consistente, detén el servidor y respalda conjuntamente `data/` y `uploads/`; no respaldes solamente el código.

## Configuración

| Variable | Valor predeterminado | Uso |
|---|---|---|
| FRONTEND_ENABLED | true | Angular como interfaz; false activa las vistas JSP conservadas |
| SERVER_ADDRESS | 127.0.0.1 | Escucha local; no expone el campus a la red |
| SERVER_PORT | 8080 | Puerto HTTP |
| DB_URL | jdbc:h2:file:./data/inudi;DB_CLOSE_ON_EXIT=FALSE | Base H2 persistente |
| DB_USER | sa | Usuario de la base local |
| DB_PASSWORD | vacío | Contraseña local; no subir secretos |
| UPLOAD_DIRECTORY | ./uploads | Almacenamiento privado |
| PUBLIC_URL | http://127.0.0.1:8080 | Destino del QR del certificado |
| DEMO_ENABLED | true | Datos y avisos de demostración |
| BOOTSTRAP_ADMIN_EMAIL | vacío | Primera cuenta administrativa de una base nueva sin demostración |
| BOOTSTRAP_ADMIN_PASSWORD | vacío | Contraseña inicial de al menos 12 caracteres, hasta 72 bytes |

Para empezar sin datos ficticios, configura DEMO_ENABLED=false y una **base H2 nueva con otra ruta**, junto con las credenciales de arranque mediante variables de entorno. No basta con desactivar la bandera en la base existente: las cuentas y los datos ya creados se conservan. Cambiar de motor de base de datos requiere incorporar el controlador correspondiente, adaptar el SQL y volver a ejecutar las pruebas; no se declara compatibilidad con otro motor sin esa verificación.

Esta entrega es local. Un despliegue público requiere configuración de HTTPS, cookies seguras, secretos propios, control de abuso, política de archivos, respaldo y revisión institucional del contenido y de la certificación. No se ha desplegado ni conectado a servicios institucionales reales.

## Pruebas y documentación

```powershell
mvn -B test
npm run test:ui
npm --prefix frontend run install:browser
npm run test:e2e
```

Las pruebas de navegador requieren el WAR construido mediante los pasos anteriores. Inician su propio servidor en 127.0.0.1:8093 con H2 en memoria y archivos en `target/e2e-uploads`; lo cierran al finalizar. No utilizan la base local. Las evidencias se generan en `target/e2e-evidence` y los resultados en `frontend/test-results`.

Las pruebas usan bases H2 en memoria distintas de la base local. Las pruebas HTTP levantan Tomcat en un puerto aleatorio y compilan realmente las JSP. Los resultados reproducibles se guardan en `target/surefire-reports/`.

El Avance 2 tenía 71 pruebas satisfactorias. La versión Angular añade verificaciones de contratos REST, sesión/CSRF, permisos, formularios, recuperación ante errores y recorridos completos en Chromium. Los resultados de la revisión actual se documentan en [PRUEBAS.md](docs/PRUEBAS.md).

- [Arquitectura y dependencias](docs/ARQUITECTURA.md).
- [Requisitos y trazabilidad](docs/REQUISITOS.md).
- [Modelo y diccionario de datos](docs/MODELO_DATOS.md).
- [Verificación y límites](docs/PRUEBAS.md).
- [Informe del sistema integrado](docs/INFORME.md).
- [Cinco imágenes del informe](docs/imagenes_informe).

## Repositorio

La raíz Git es esta carpeta dentro de `avance_3` y el remoto origin apunta a `https://github.com/wsucari/utp_desarrollo_web_integrado.git`. El trabajo del APF3 se realiza en `main`; `avance_2` conserva el commit 2836441 de la segunda entrega. Las nuevas modificaciones solo se publicarán cuando el usuario lo solicite. Los datos y archivos de demostración no deben incorporarse al control de versiones.

La integración Angular/API REST fue publicada por solicitud del usuario en el commit `6208a8b`. El informe y sus cinco figuras se conservan junto con la documentación técnica. Los resultados de pruebas mantienen el estado registrado al ejecutar cada verificación.

## Desarrollo Angular y contratos REST

El código Angular está en `frontend/src/app`, organizado en componentes de acceso, catálogo, paneles, administración, gestión, aula y portal del estudiante. `core` contiene modelos, cliente HTTP, sesión, interceptor CSRF y guard; `shared` reúne modales reactivos, esquemas de formulario y componentes reutilizables. Las rutas utilizan carga diferida.

Para editar Angular con recarga automática, ejecuta el backend en 8080 y, desde otra terminal, `npm --prefix frontend start`. El proxy mantiene las llamadas `/api`, los estilos y las imágenes en el mismo origen del navegador. Para la ejecución integrada, `npm run build:ui` compila Angular en `src/main/resources/static/spa`; el WAR sirve la interfaz y la API desde 8080. Los compilados Angular se generan localmente y no se versionan.

[Contratos de la API y seguridad](docs/APF3_API.md). [Arquitectura actual](docs/ARQUITECTURA.md).
