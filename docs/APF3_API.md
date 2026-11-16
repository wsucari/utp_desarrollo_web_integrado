# API REST del Avance 3

La API está bajo `/api`. Angular y Spring Boot se sirven desde el mismo origen. Los controladores reutilizan AcademicService, CatalogService, SessionService, almacenamiento privado y generación PDF. Las entradas JSON utilizan DTO y Jakarta Validation; ApiViews define los campos de salida sin serializar entidades JPA directamente.

## Sesión y CSRF

1. GET `/api/auth/csrf` devuelve `token` y `headerName` y establece la sesión anónima cuando sea necesaria.
2. POST `/api/auth/login` recibe `{email, password}` en JSON y el encabezado CSRF. Verifica BCrypt, cambia el identificador de sesión, guarda el contexto y registra el acceso. Devuelve la cuenta actual, la bandera de demostración y el contador del carrito.
3. Después del acceso se obtiene nuevamente CSRF, porque el token anterior se invalida. Las cookies las administra el navegador y no se exponen a JavaScript.
4. El interceptor añade el encabezado CSRF solo a escrituras de `/api/`. No se almacenan credenciales ni tokens de autenticación en localStorage.
5. GET `/api/auth/session` consulta la identidad actual. POST `/api/auth/logout` registra la salida e invalida la sesión; Angular obtiene un nuevo contexto anónimo.

No se agregó JWT. Los guards orientan la navegación; Spring Security y los servicios vuelven a comprobar rol, cuenta activa, asignación docente, titularidad y vigencia. El proxy de desarrollo evita configurar CORS abierto. La API autenticada responde con JSON 401/403, mientras las rutas JSP conservadas mantienen sus redirecciones.

## Operaciones

Los métodos de creación devuelven 201; las consultas y actualizaciones con cuerpo, 200; las acciones sin cuerpo y las eliminaciones, 204. No se elimina historial académico para satisfacer una petición DELETE.

| Grupo | Rutas | Métodos y acceso |
|---|---|---|
| Acceso | `/auth/csrf`, `/auth/session`, `/auth/login`, `/auth/logout`, `/auth/registro` | GET de contexto; POST de acceso, salida y registro. CSRF obligatorio en POST. Registro público crea estudiantes independientemente del rol recibido |
| Catálogo | `/cursos`, `/cursos/{id}` | GET público de cursos publicados; filtros search/q, ruta, categoria/category, nivel y acceso. Un gestor puede consultar su curso sin publicar |
| Panel y perfil | `/panel`, `/perfil` | GET autenticado; PUT de perfil y cambio de contraseña validando la actual |
| Selección | `/carrito`, `/carrito/{id}`, `/carrito/solicitar` | GET de selección; POST para añadir/solicitar; DELETE para retirar. Estudiante |
| Historial del estudiante | `/mis-cursos`, `/mis-pagos`, `/comentarios` | GET de registros propios |
| Usuarios | `/admin/usuarios`, `/admin/usuarios/{id}`, `/admin/profesores` | GET, POST de alta y PUT de edición. Administrador |
| Cursos | `/admin/cursos`, `/admin/cursos/{id}`, `/gestion/cursos`, `/gestion/cursos/{id}` | GET de gestión; POST/PUT/DELETE administrativos. Eliminación solo si no existen relaciones que deban conservarse |
| Opciones administrativas | `/admin/opciones` | GET de estudiantes, docentes activos, cursos y fecha inicial para formularios. Administrador |
| Pagos | `/admin/pagos`, `/admin/pagos/{id}`, `/admin/pagos/{id}/confirmar`, `/admin/pagos/{id}/anular` | GET, POST de alta, PUT de edición y POST de acciones. Administrador. Edición incluye version y conserva estudiante/curso |
| Matrículas | `/admin/matriculas`, `/admin/matriculas/{id}/cancelar` | GET y POST de cancelación conservando historial. Administrador |
| Consultas administrativas | `/admin/resenas`, `/admin/sesiones`, `/admin/auditoria`, `/admin/certificados` | GET. Sesiones admite search y sesion. Administrador |
| Vista de perfiles | `/admin/vista-previa`, `/admin/vista-previa/{profile}/{id}` | GET de solo lectura; filtros search y roleFilter. No cambia la identidad del administrador |
| Módulos | `/gestion/cursos/{id}/modulos`, `/gestion/cursos/{courseId}/modulos/{id}`, `/gestion/modulos/{id}` | POST, PUT, GET y DELETE. Gestor autorizado para el curso |
| Contenidos | `/gestion/modulos/{id}/contenidos`, `/gestion/modulos/{moduleId}/contenidos/{id}`, `/gestion/contenidos/{id}` | POST, PUT y DELETE. Gestor autorizado |
| Material | `/gestion/contenidos/{id}/material`, `/contenidos/{id}/archivo` | POST multipart con file y GET binario privado; autorización de gestión o acceso vigente, respectivamente |
| Cuestionarios | `/gestion/contenidos/{id}/preguntas`, `/gestion/contenidos/{contentId}/preguntas/{id}`, `/gestion/preguntas/{id}` | GET/POST, PUT y DELETE. Gestor del curso; bloqueos de edición cuando hay intentos |
| Aula | `/aula/cursos/{id}`, `/contenidos/{id}`, `/vista-previa/{id}` | GET. Los cuerpos de contenido requieren acceso o vista previa autorizada |
| Progreso | `/contenidos/{id}/completar` | POST idempotente de estudiante con acceso vigente. No exige entregar, publicar ni aprobar para marcar un contenido |
| Actividades | `/contenidos/{id}/entregar`, `/entregas/{id}/archivo` | POST multipart con file/comment y GET de descarga autorizada |
| Calificación | `/gestion/entregas/{id}/calificar` | PUT JSON con grade y feedback. Gestor del curso; nota entre 0 y 20 |
| Evaluaciones | `/contenidos/{id}/evaluar`, `/evaluaciones/intentos/{id}` | POST de respuestas y GET de resultado autorizado. Respuestas como objeto `q_ID: [respuesta1, respuesta2]`, en el orden de los espacios/relaciones |
| Publicaciones | `/contenidos/{id}/publicaciones`, `/publicaciones/{id}` | POST de aporte/respuesta, PUT de texto propio y DELETE de retiro lógico |
| Interacciones | `/publicaciones/{id}/reaccion`, `/publicaciones/{id}/valorar` | POST con type o score. Se conservan reglas de reacción, valoración y autoría |
| Reseñas de curso | `/cursos/{id}/resenas`, `/resenas/{id}` | POST con score/body y DELETE autorizado |
| Seguimiento | `/gestion/cursos/{id}/seguimiento`, `/gestion/cursos/{id}/reporte.csv` | GET de datos y descarga CSV. Gestor asignado; protección de fórmulas en celdas CSV |
| Certificación | `/certificados`, `/certificados/emitir/{id}`, `/certificados/{id}/pdf`, `/cert`, `/cert/{code}` | GET de certificados propios, POST idempotente de emisión, GET PDF autorizado y consulta autenticada de estudiantes |

Los cuerpos de altas/ediciones conservan los campos de AccountForm, CourseForm, ContentForm, QuestionForm y PaymentForm. La actualización del pago requiere su version actual. Los contenidos de evaluación conservan los cinco tipos de pregunta, los intentos configurados y el umbral de aprobación existentes.

## Respuestas y protección de datos

- Nunca se incluyen hashes de contraseñas, claves internas de archivos ni entidades con relaciones recursivas.
- El temario público contiene títulos, tipos y orden; el cuerpo y recurso de una lección se obtienen solo después de comprobar acceso/vista previa.
- Las preguntas del estudiante omiten solución y explicación. En relaciones se separan etiquetas y opciones sin publicar los pares resueltos. Las respuestas esperadas se muestran en resultados autorizados y en la gestión docente.
- Las descargas conservan Content-Disposition y los permisos de titularidad/curso.
- Los errores utilizan `{status, message, errors}`. Los errores de campo se conservan en el modal junto con los valores ingresados.

| Estado | Significado |
|---|---|
| 400 | Entrada JSON, formato o campo inválido |
| 401 | Falta sesión válida o credenciales incorrectas |
| 403 | Rol/recurso no autorizado o CSRF ausente/inválido |
| 404 | Recurso solicitado inexistente |
| 409 | Conflicto de integridad o actualización concurrente |
| 413 | Archivo por encima del tamaño permitido |
| 422 | Regla académica/administrativa no satisfecha o archivo no procesable |

## Ejecución y evidencias

`npm run build:ui` compila Tailwind y Angular; `mvn -B package` ejecuta las pruebas Java y genera el WAR integrado. `npm run test:ui` comprueba cliente HTTP, sesión, guards y formularios. `npm run test:e2e` utiliza Chromium con un servidor real y H2 en memoria, sin tocar los datos locales.

La interfaz mantiene `/` como catálogo; los alias `/cursos` y `/catalogo` conservan filtros. Las rutas existentes de formulario abren su listado/aula con modal. Las JSP permanecen para la suite histórica y pueden activarse con FRONTEND_ENABLED=false.

Referencias: [Angular HttpClient](https://angular.dev/guide/http), [formularios reactivos](https://angular.dev/guide/forms/reactive-forms), [guards](https://angular.dev/guide/routing/route-guards), [CSRF para aplicaciones JavaScript en Spring Security 6.5](https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html).
