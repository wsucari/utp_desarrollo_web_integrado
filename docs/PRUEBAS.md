# Verificación de la implementación

## Ejecución reproducible

Con JDK 21 seleccionado:

```powershell
npm ci
npm run build:css
mvn -B test
```

Las pruebas utilizan H2 en memoria; no leen ni modifican `data/inudi.mv.db`. Las cargas HTTP de prueba se guardan en `target/http-test-uploads/`. Los reportes se generan en `target/surefire-reports/`.

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
