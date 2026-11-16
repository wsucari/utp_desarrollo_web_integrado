# Verificación de la implementación

## Ejecución reproducible

Con JDK 21 seleccionado:

```powershell
npm ci
npm --prefix frontend ci
npm run build:ui
mvn -B package
npm run test:ui
npm --prefix frontend run install:browser
npm run test:e2e
```

Las pruebas utilizan H2 en memoria; no leen ni modifican `data/inudi.mv.db`. Las cargas HTTP de prueba se guardan en `target/http-test-uploads/`. Los reportes Java se generan en `target/surefire-reports/`. Playwright inicia el WAR en 127.0.0.1:8093 con otra base en memoria y archivos en `target/e2e-uploads`; lo detiene al terminar. Los resultados están en `frontend/test-results/results.json` y las capturas en `target/e2e-evidence`. El puerto 8093 debe estar libre. Los resultados históricos siguientes se conservan y la verificación APF3 figura al final.

## Tipos de prueba

- **ExamGraderTest:** coincidencia de opciones, texto normalizado, relaciones, espacios, verdadero/falso, respuestas faltantes y límites de estrellas.
- **AcademicIntegrationTest:** persistencia, consultas JPQL, autorización por rol/recurso, vigencia, progreso, entregas, notas, ponderación, cuestionarios, participación, pagos, certificados y CRUD.
- **PaymentAcademicRulesTest:** registro y edición manual de pagos, sincronización de vigencia, conflicto de versión, conservación de identidad/historial, permisos, progreso independiente de entregas/aportes/aprobación, siguiente contenido, examen inactivo, umbral cero e intentos ilimitados.
- **StorageServiceTest:** nombres generados, lectura privada, rechazo de ejecutables, extensión falsa, recorridos de ruta y tamaño de archivos.
- **CertificatePdfServiceTest:** PDF con acentos, código, QR decodificable y texto largo en una página.
- **ConcurrentAttemptsTest:** seis operaciones simultáneas; se permiten exactamente tres intentos, con números 1, 2 y 3 sin duplicados.
- **MvcHttpSmokeTest:** Tomcat real, login con cookies y CSRF, compilación de las JSP de los tres perfiles, UTF-8, carga multipart, CSV y recorrido completo. Incluye comprobación de que el acceso carga la hoja Tailwind compilada, presenta la tarjeta y tiene el control de mostrar contraseña.

La prueba HTTP integral entrega una actividad, registra una calificación docente, publica un aporte escapado, resuelve una evaluación, registra todos los contenidos, emite y descarga un certificado y consulta su código como estudiante autenticado en /cert. También comprueba que otro estudiante no pueda descargar ese certificado.

## Desarrollo guiado por pruebas

Se ejecutó un ciclo inicial sobre la corrección de evaluaciones: se escribieron los seis casos de ExamGraderTest, se verificó el fallo por ausencia de la clase y luego se implementó la corrección hasta obtener los seis casos satisfactorios. Las pruebas de integración y HTTP se añadieron para comprobar el conjunto y detectar defectos de renderizado y navegación. No se afirma que cada línea del sistema haya seguido estrictamente un ciclo TDD independiente.

## Evidencia y límites

La ejecución inicial del 12 de octubre de 2026 obtuvo 51 pruebas satisfactorias. Tras incorporar Tailwind y la comprobación del acceso, la suite obtuvo **52 pruebas, 0 fallos, 0 errores y 0 omitidas**. Los reportes reproducibles están en Maven. El número de pruebas no representa por sí solo un porcentaje de cobertura de código.

Después de corregir cabeceras, menús por rol, rutas y catálogo, la ejecución completa obtuvo **55 pruebas, 0 fallos, 0 errores y 0 omitidas**. Se comprueban nombres de navegación, logotipos servidos, ausencia de frases añadidas, destinos y permisos por perfil, filtros originales, consulta de perfiles sin cambio de identidad e historial real de login/logout. Inicio y cierre de sesión devuelven 303 para continuar mediante GET.

Tras la unificación de cabecera solicitada por el usuario, la suite obtuvo **57 pruebas, 0 fallos, 0 errores y 0 omitidas**. Los casos añadidos verifican la misma cabecera para visitantes y los tres roles, ausencia de enlaces de navegación en ella, orden exacto de los ocho enlaces administrativos en el sidebar, identificadores distintos para los desplegables de escritorio/móvil y redirección de /cursos y /catalogo a / preservando filtros. Se ajustó el caso de catálogo público para comprobar la nueva ruta canónica. La verificación visual en 390 × 844 comprobó hamburguesa, usuario/desplegable y cierre con Escape, además del regreso al lateral de escritorio.

Estas pruebas verifican el comportamiento de la implementación actual. **No prueban que sea una réplica fiel del sistema entregado.** La revisión del usuario detectó diferencias de pantallas, navegación y procesos, registradas en PARIDAD_FUNCIONAL.md. Se requieren pruebas de equivalencia derivadas del original antes de declarar concluida la réplica.

No se han realizado pruebas de carga masiva, auditoría externa de seguridad, antivirus, conectividad institucional ni despliegue público. Correo, pasarelas y Google Drive están excluidos por indicación del usuario y no se consideran pruebas faltantes del alcance vigente. No se inventan porcentajes de aprobación docente ni evidencia de commits, ramas, fusiones o incidencias de GitHub que no se hayan producido.

## Revisión UI/UX posterior

La suite completa obtuvo 58 pruebas satisfactorias tras mover la cuenta móvil detrás de la navegación, usar un desplegable ascendente y normalizar la interfaz. El caso nuevo comprueba el orden del HTML y las reglas compiladas del pie del sidebar, estados de navegación, redondeado mínimo y ajustes responsivos.

La revisión de navegador incluyó 31 mediciones de páginas de los tres perfiles y el catálogo, en 320/390/768/1280 px: sin desbordamiento horizontal del documento ni controles principales fuera del contenido en las muestras comprobadas. Se verificaron foco/cierre del menú, permanencia del pie inferior y selección de docentes con casillas. Se inspeccionaron curso, usuario, perfil, pagos, contenido, cuestionario, seguimiento, actividad, foro y evaluación. No equivale a cobertura exhaustiva de dispositivos ni a finalización de la réplica.

## Modales, pagos manuales y reglas académicas

La compilación completa del 12 de octubre de 2026 obtuvo **71 pruebas, 0 fallos, 0 errores y 0 omitidas**: 31 AcademicIntegrationTest, 12 PaymentAcademicRulesTest, 14 MvcHttpSmokeTest, 6 ExamGraderTest, 5 StorageServiceTest, 2 CertificatePdfServiceTest y 1 ConcurrentAttemptsTest. Se comprueba también el formato ISO de la fecha del formulario de pago, evitando un campo vacío en el navegador.

La revisión real abrió creación/edición de cursos, usuarios, módulos, contenidos, preguntas y pagos sin abandonar su pantalla. Se guardó una edición sin cambio de importe/referencia en un pago de demostración y se confirmó el regreso al mismo listado. Un intento inválido de cambiar un pago confirmado mostró el motivo dentro del modal y conservó sus datos. El alta de pago confirmado y su matrícula se verificaron en la prueba HTTP con base aislada.

El modal de pago se inspeccionó en escritorio y en 390 × 844: ancho exterior de 374 px, área interna de 372 px sin desbordamiento horizontal y campos/acciones adaptables. Evidencias: `docs/evidencias/modal-registrar-pago.jpg` y `docs/evidencias/modal-pago-movil.jpg`. Esta muestra no acredita todos los dispositivos ni termina los perfiles/reseñas diferidos.

## APF3: Angular, API REST e integración — 16 de noviembre de 2026

Se ejecutó la verificación desde la copia `avance_3/inudi_virtual_springboot`, en `main`. El frontend de producción se compiló con Angular 22 y se incluyó en el WAR Spring Boot 3.5.16. La suite de navegador consumió exclusivamente ese WAR, con sus recursos empaquetados, desde una base H2 nueva en memoria.

| Verificación ejecutada | Resultado |
|---|---|
| `npm run build:ui` | Tailwind y Angular compilados; carga diferida de páginas |
| `mvn -B package` | 85 pruebas, 0 fallos, 0 errores y 0 omitidas; WAR generado |
| `npm run test:ui` | 15 pruebas, todas satisfactorias |
| `npm run test:e2e` | 4 recorridos Chromium satisfactorios, sin omisiones ni reintentos |
| Arranque habitual y desarrollo | Catálogo, acceso administrativo, ocho enlaces y cierre de sesión comprobados en 8080 y mediante el proxy Angular en 4200; sin errores JavaScript en estas muestras |

Las 85 pruebas Java se distribuyen en AcademicIntegrationTest (31), PaymentAcademicRulesTest (12), ExamGraderTest (6), StorageServiceTest (5), CertificatePdfServiceTest (2), ConcurrentAttemptsTest (1), MvcHttpSmokeTest (15) y ApiIntegrationTest (13). Se conservaron los casos anteriores; la comprobación HTTP histórica sigue compilando JSP con `app.frontend.enabled=false`.

ApiIntegrationTest comprueba contratos JSON seguros, validaciones, autenticación, CSRF, roles, asignación de docente, cuentas inactivas, respuestas de evaluaciones y permisos sobre recursos. La inscripción pública no permite elevar el rol a administrador. El temario público no revela cuerpos o archivos privados, contraseñas ni soluciones. Se conserva la regla anterior del nombre de perfil, sin imponer un mínimo nuevo de caracteres. Un caso HTTP adicional inicia sesión realmente con cookies, mantiene CSRF durante doce escrituras consecutivas y elimina el curso aislado de prueba.

Las pruebas Angular verifican peticiones y filtros, errores de conexión/servidor, inicio y cierre de sesión, renovación CSRF, reintento de inicialización y guards. Los modales conservan datos ante errores, validan límites, mantienen identidades deshabilitadas y crean correctamente el formulario al reabrir un esquema. Este último caso detectó y corrigió la reutilización del formulario anterior al crear varios contenidos consecutivos.

Los cuatro recorridos de navegador comprueban lo siguiente:

1. Catálogo con datos reales, filtros, resultado vacío, fallo 503 simulado, recuperación y alias con filtros conservados.
2. Creación administrativa de curso, docente asignado, módulo y seis tipos de contenido; material privado y cinco tipos de pregunta; usuario duplicado con datos conservados; creación/edición de pago confirmado y rechazo de un estado inválido; acceso del estudiante, finalizaciones independientes, descarga de material, entrega de actividad, foro con edición/respuesta/reacción, evaluación de cinco tipos con resultado de 100 %, calificación docente de 18.5, CSV, emisión/descarga/validación de PDF y rechazo de descargas ajenas con 403.
3. Rutas actuales de administración, docente y estudiante; filtros; vista previa de solo lectura sin cambiar la identidad administrativa; perfil con persistencia tras recargar; navegación denegada por rol y registro público con mensaje conservado al llegar al acceso.
4. Carrito, solicitud pendiente y mensaje de confirmación; el acceso al aula permanece denegado hasta la validación administrativa del pago.

Los perfiles se revisaron en escritorio y el menú de los tres roles también en 390 × 844. Se comprobó la cuenta al pie del menú móvil, desplegable hacia arriba y ausencia de desbordamiento horizontal de página en las muestras medidas. Se inspeccionaron visualmente capturas de administración, menú móvil, evaluación y solicitud pendiente.

El resumen verificable está en [apf3-verificacion.json](evidencias/apf3-verificacion.json). Las capturas de esta ejecución llevan el prefijo `angular-` en `docs/evidencias`. Los registros detallados regenerables se conservan localmente en `target/apf3-package.log`, `target/frontend-tests.log` y `target/e2e-final.log`, excluidos de Git.

La comprobación cubre los flujos indicados en Chromium y H2 local; no acredita otros motores, todos los navegadores, dispositivos físicos ni equivalencia integral con la referencia funcional. Los límites históricos de perfiles/reseñas, imágenes, páginas institucionales, filtros/paginación y detalle de sesiones se mantienen en PARIDAD_FUNCIONAL.md. No se añadieron pasarelas, Drive o correo ni se realizó despliegue público. No se modificó el informe formal ni se publicaron commits.

### Revisión adicional solicitada después de la implementación

El 16 de noviembre de 2026 se repitieron las suites completas: **85 pruebas Java, 15 Angular y 5 recorridos Chromium satisfactorios**, sin fallos ni omisiones en la ejecución final. Se añadió `frontend/e2e/crud.spec.ts`: edita y recarga cursos, módulos, contenidos y preguntas para verificar persistencia; comprueba el rechazo del borrado de un curso con un módulo; elimina pregunta, contenido, módulo y curso vacíos; confirma la ausencia tras recargar y la respuesta 404 del recurso eliminado. Este recorrido usa únicamente datos ficticios en la base aislada de Playwright.

También se verificó la aplicación activa en 8080: inicio/cierre de sesión y once pantallas con recarga directa de administrador, docente y estudiante, sin errores JavaScript. El catálogo respondió 200 y entregó el documento Angular. La revisión no requirió cambiar el código de producción; se amplió la cobertura automatizada y se conservaron los pendientes de alcance ya descritos.

Resumen de esta revisión: [apf3-revision.json](evidencias/apf3-revision.json). Registros locales regenerables: `target/revision-java.log`, `target/revision-angular.log` y `target/revision-e2e.log`. La rama activa continúa siendo `main`; el respaldo `avance_2` y la carpeta original no fueron modificados.
