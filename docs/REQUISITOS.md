# Requisitos verificables de la implementación local

Estos requisitos identifican el comportamiento efectivamente desarrollado. Son la base técnica para la redacción posterior del APF2, no sustituyen el informe académico ni afirman una cobertura porcentual docente.

## Requisitos funcionales y trazabilidad

| ID | Comportamiento | Evidencia de implementación | Verificación |
|---|---|---|---|
| RF-01 | Iniciar/cerrar sesión y restringir funciones según el perfil | SecurityConfig, /login, /logout | Pruebas de acceso por rol y login HTTP |
| RF-02 | Registrar estudiantes y administrar usuarios/docentes activos | PublicController, AdminController, AccountForm | Registro, formularios y protección del último administrador |
| RF-03 | Actualizar el perfil y cambiar contraseña validando la actual | PortalController, /perfil | Servicio con validación, BCrypt y formulario JSP |
| RF-04 | Buscar cursos publicados por título, categoría, nivel y ruta académica | CourseRepository.catalog, / | Consulta JPQL y página pública; alias /catalogo redirige a / |
| RF-05 | Crear, editar, publicar u ocultar cursos y asignar docentes | AdminController, CourseForm | CRUD de curso; eliminación segura de curso vacío |
| RF-06 | Organizar módulos y contenidos ordenados | ManagementController, LearningModule y Content | JSP de gestión y repositorios ordenados |
| RF-07 | Consultar textos, videos, materiales y vista previa | LearningController, /contenidos y /vista-previa | HTTP de seis tipos y control de acceso |
| RF-08 | Seleccionar cursos y solicitar matrícula sin cobrar automáticamente | CartItem, Payment, /carrito/solicitar | Servicio de solicitud y estado PENDIENTE |
| RF-09 | Registrar y editar pagos manuales desde el listado, sincronizando el acceso de forma atómica | PaymentForm, savePayment, Payment y Enrollment | Alta, edición, versión concurrente, confirmación y vencimiento válido |
| RF-10 | Controlar vigencia y cancelar acceso conservando historial | Enrollment, revokeEnrollment, lockedAccess | Matrícula vencida y conservación del progreso |
| RF-11 | Registrar avance único por contenido y calcular porcentaje | Progress, complete y percent | Idempotencia y curso completado por HTTP |
| RF-12 | Recibir entregas privadas de actividades con intentos limitados | Submission, StorageService | Límite de entregas y multipart real con CSRF |
| RF-13 | Calificar entre 0 y 20 y comunicar retroalimentación | grade, /gestion/entregas/{id}/calificar | Nota válida, nota rechazada y recorrido HTTP |
| RF-14 | Configurar cinco tipos de pregunta con puntajes | Question, QuestionForm, /gestion/contenidos/{id}/preguntas | Gestión JSP y corrección de los cinco tipos |
| RF-15 | Activar/desactivar evaluaciones, corregir respuestas ponderadas y configurar intentos limitados o ilimitados | ExamGrader, evaluate, ExamAttempt/Answer | Normalización, ponderación, examen inactivo, límites, intento 21 y concurrencia |
| RF-16 | Mostrar historial y explicación de resultados propios | /evaluaciones/intentos/{id} | Resultado HTTP y rechazo a otro estudiante |
| RF-17 | Publicar, responder, editar y retirar aportes sin perder respuestas | Discussion y servicios de participación | Respuesta del mismo contenido y retiro lógico |
| RF-18 | Reaccionar, valorar aportes ajenos y reseñar cursos | Reaction, Rating y CourseReview | Alternancia de reacción, actualización y rechazo de autovaloración |
| RF-19 | Emitir un certificado único con PDF y QR; consultar el código con sesión de estudiante | Certificate, CertificatePdfService, /cert | Unicidad, requisito 100 %, lectura QR, permisos y descarga HTTP |
| RF-20 | Consultar/exportar seguimiento y operaciones relevantes | /seguimiento, /reporte.csv, /admin/auditoria | JSP de docente, CSV y registros de operaciones |
| RF-21 | Crear y editar registros en modales sobre la pantalla actual, conservando datos ante errores | editor-modal.jspf, app.js y formularios MVC | Pruebas HTTP y navegación real; pagos sin acordeón |
| RF-22 | Mostrar el siguiente contenido sin completar y acumular estrellas de intentos reales | enrollmentRow, dashboard y ExamAttempt | Siguiente contenido, intento ilimitado y reglas de estrellas |

La tabla distingue pruebas automatizadas de evidencias de implementación y de navegación. Una fila no implica que todas las combinaciones posibles del requisito tengan cobertura automatizada.

## Reglas de negocio

| ID | Regla |
|---|---|
| RN-01 | Solo estudiantes activos realizan solicitudes y operaciones de aprendizaje |
| RN-02 | La solicitud pendiente no habilita acceso; el registro administrativo confirmado activa la matrícula y define su vencimiento en la misma transacción |
| RN-03 | Un docente solo gestiona los cursos a los que está asignado |
| RN-04 | La vigencia se calcula al consultar; vencer no borra matrícula, progreso ni pagos |
| RN-05 | El progreso registra una única finalización por contenido y estudiante |
| RN-06 | Un estudiante con matrícula vigente puede marcar un contenido como completado; esta operación no exige entrega, aporte en foro ni aprobación de examen |
| RN-07 | Las actividades admiten de 1 a 10 entregas; las evaluaciones admiten de 1 a 20 intentos o 0 para intentos ilimitados. Un examen inactivo no permite nuevos intentos |
| RN-08 | La nota de actividad está entre 0 y 20; la evaluación expresa un porcentaje ponderado por puntos |
| RN-09 | Las estrellas corresponden a 50 % (una), 70 % (dos) y 90 % (tres); por debajo de 50 % son cero |
| RN-10 | Las respuestas del foro pertenecen al mismo contenido; retirar un aporte conserva sus respuestas |
| RN-11 | Cada usuario tiene una reacción y una valoración actual por publicación; no puede valorar su propia publicación |
| RN-12 | Una evaluación con intentos no admite cambiar o eliminar preguntas ni modificar su umbral de aprobación |
| RN-13 | El certificado requiere matrícula vigente, certificación habilitada y progreso al 100 %; la emisión repetida devuelve el mismo registro |
| RN-14 | No se elimina físicamente un curso o contenido con historial académico |
| RN-15 | Debe permanecer al menos un administrador activo |
| RN-16 | La descarga de materiales exige acceso al curso; las entregas y resultados son propios o de docentes asignados/administración |
| RN-17 | El umbral de aprobación de una evaluación está entre 0 y 100; las estrellas se acumulan a partir de intentos registrados, sin valores ficticios |
| RN-18 | Editar un pago conserva estudiante y curso. Un pago confirmado conserva su estado; un pago anulado no se reactiva. Importe, referencia y vigencia se editan sin duplicar matrícula, con control de versión |

Las reglas de progreso e intentos se cotejaron con el funcionamiento de referencia. No se convierten en barreras nuevas para completar contenidos ni se presentan como un reglamento institucional adicional.

## Alcance vigente confirmado por el usuario

Los pagos son registros administrativos manuales. No hay pasarelas, Google Drive ni envío de correo: los archivos permanecen en el almacenamiento privado del sistema y los certificados se descargan. Estas integraciones están excluidas, no pendientes. La ampliación de perfiles y reseñas queda para una etapa posterior; RF-03 y RF-18 describen únicamente lo disponible, no una equivalencia completa de esas pantallas.

## Requisitos no funcionales comprobables

| ID | Condición | Evidencia |
|---|---|---|
| RNF-01 | Persistencia relacional y esquema versionado sin recreación al iniciar | H2 de archivo, Flyway, Hibernate validate |
| RNF-02 | Contraseñas protegidas, CSRF y autorización por recurso | SecurityConfig y pruebas de seguridad |
| RNF-03 | No exponer archivos académicos mediante rutas estáticas públicas | StorageService y controladores de descarga |
| RNF-04 | Límite de 10 MB por archivo; formatos restringidos | Configuración multipart y pruebas de almacenamiento |
| RNF-05 | Formularios validados y errores sin trazas internas | Jakarta Validation y WebAdvice |
| RNF-06 | Textos y vistas en español UTF-8 | Directivas JSP y pruebas HTTP de codificación |
| RNF-07 | Separación de presentación, reglas, persistencia y componentes auxiliares | Paquetes web, service, repository y domain |
| RNF-08 | Las pruebas no utilizan ni alteran la base local de demostración | Bases H2 en memoria y almacenamiento de pruebas en target |
| RNF-09 | Integridad ante envíos simultáneos de evaluaciones | Bloqueos transaccionales y prueba de seis envíos concurrentes |

No se han establecido garantías cuantitativas de rendimiento, disponibilidad o capacidad de producción sin una medición específica.
