# Auditoría inicial de fidelidad funcional

Fecha: 12 de octubre de 2026. Documento interno de trabajo; no forma parte de la redacción académica de INFORME.md.

## Corrección del objetivo

El usuario pidió replicar el sistema entregado con Java/Spring Boot/MVC/JSP, no elaborar una aplicación alternativa. La primera implementación conservó parte de los módulos, pero rediseñó la interfaz y simplificó o modificó procesos sin verificar su equivalencia. Tener pruebas satisfactorias no demuestra fidelidad al sistema original. Esta auditoría inicial no es todavía un inventario exhaustivo.

## Diferencias comprobadas

Las referencias siguientes son rutas de archivos relativas a la carpeta `inudi_virtual_laravel` del espacio de trabajo. Se consultó código y plantillas; no se ejecutaron cobros, envíos de correo ni integraciones externas.

| Área | Referencia comprobada | Java actual | Diferencia que debe resolverse |
|---|---|---|---|
| Entrada | `routes/web.php`: `/` redirige a `login` | PublicController devuelve una portada nueva | La portada no reproduce la entrada original |
| Acceso | `resources/views/auth/login.blade.php` y `components/authentication-card.blade.php`: tarjeta centrada, fondo gris, título Plataforma Inudi y mostrar contraseña | Primera versión: composición de dos columnas y navegación pública | Se sustituyó por tarjeta con Tailwind en esta corrección; faltan logotipo y comparación visual completa |
| Navegación y rutas | `/dashboard`, `/admin/user`, `/admin/profesores`, `/users/panel/...`, `/profesor/...` | `/panel`, usuarios/docentes reunidos, `/gestion/...` | Se fusionaron pantallas y se renombraron recorridos sin comprobar equivalencia |
| Certificados | `routes/web.php`: `/cert` y `/cert/{codigo}` dentro del grupo de estudiantes autenticados; `CertificadoController::verCertificado` consulta el código | `/verificar` y `/verificar/{code}` públicos con enlaces en cabecera y menú | La consulta existe en la referencia, pero se cambió su ubicación y acceso. No eliminar todo el módulo por el distinto nombre |
| QR de certificado | `CertificadoController::generarCertificado` apunta a `/validar-certificado/{codigo}`; ese destino no figura en `routes/web.php` consultado | QR apunta a `/verificar/{code}` | La referencia contiene una inconsistencia de destino; no tratarla como evidencia de una ruta pública operativa ni copiarla sin aclaración |
| Emisión | `CertificadoController`: genera PDF y lo envía mediante CertificadoCursoMail | Genera y ofrece descarga local | La descarga no equivale al flujo de envío por correo |
| Pago | `PagosController` y rutas: PayPal, Culqi, retornos y generación de boleta | Carrito crea solicitud pendiente que confirma administración | Se sustituyó el proceso. Que exista código de integración no demuestra que las credenciales o servicios funcionen en este entorno |
| Archivos | `DriveFileController`: GoogleDriveService para materiales y entregas, con controles de acceso | StorageService guarda archivos locales | Almacenamiento e integración diferentes; no declarar Google Drive replicado |
| Vista previa de perfiles | `AdminPreviewController` y `/admin/vista-previa/estudiante/{user}` / `profesor/{profesor}`: consulta en solo lectura | Vista previa de un contenido | Son funcionalidades distintas; falta la vista previa de perfiles |
| Sesiones | `SesionAdminController`: listado, búsqueda, filtros y fechas de entrada/salida | Registro de operaciones general | La auditoría no reemplaza el historial de sesiones |
| Perfil | `PanelUserController` y rutas de foto/datos/password; rutas análogas para profesor | Formulario limitado de datos | Falta comprobar y reproducir los formularios separados y la carga de foto |
| Panel estudiante | `PanelUserController`: cursos vigentes, siguiente contenido, cursos completados y StudentReward | Indicadores y listados distintos | No se ha reproducido el panel ni su sistema de recompensas |
| Progreso | `ProgresoController::actualizarProgreso`: registra contenido completado con acceso vigente; no exige publicación o entrega en ese método | AcademicService agrega condiciones para actividad, foro y examen | Las condiciones añadidas necesitan cotejo con las vistas y servicios originales; no asumir equivalencia |
| Autenticación social | Hay rutas Google/Facebook y controlador; los botones están comentados en la plantilla de acceso | Sin OAuth | Diferenciar código disponible de funciones visibles; no añadir botones por encontrar rutas sin más |

## Tailwind aplicado en esta corrección

- Dependencia fijada y archivo de bloqueo de npm para compilación reproducible.
- Configuración de detección de JSP/JSPF/tag y JavaScript.
- Hoja estática compilada localmente, cargada en el acceso y el encabezado común.
- Pantalla de acceso reorganizada tomando la plantilla original como referencia, con utilidades Tailwind y mostrar/ocultar contraseña.
- CSS anterior conservado para las pantallas que todavía no se han adaptado. No se considera una conversión completa por enlazar la hoja de estilos.
- La búsqueda inicial limitada a `public` fue incompleta. Los logotipos reales están en `storage/app/site-assets/images/logos`, servidos por PublicAssetController. Se copiaron esos archivos sin modificarlos a los recursos estáticos Java; ya no se usa texto temporal como sustituto del logo.

## Criterio de aceptación para la réplica

### Corrección puntual del sidebar

Se eliminó del fragmento común el bloque añadido «NUESTRO PROPÓSITO / Investigar para un mundo mejor / Conocimiento que transforma». No corresponde a un elemento solicitado para el sidebar de la réplica. Se añadió una comprobación de ausencia a las pruebas HTTP de las páginas por perfil. Esta corrección no completa la reproducción del header, logotipo, menús ni pantallas, que siguen abiertos en la auditoría.

Cada pantalla y flujo debe compararse con su ruta, controlador, plantilla y reglas de referencia. El registro deberá identificar: pantalla original, contraparte Java, campos, permisos, acciones, estados, navegación, diferencias y prueba de equivalencia. Las rutas, permisos o servicios contradictorios del original deben explicarse, no corregirse silenciosamente ni utilizarse para justificar un producto diferente.

Primero se revisarán acceso y menús por rol; después usuarios y docentes, cursos/módulos/contenidos, participación/entregas/evaluaciones, progreso y paneles, pagos y certificados. Las integraciones externas se separarán del funcionamiento local y no se declararán operativas sin comprobación segura. No se utilizarán datos personales ni secretos de la copia original para poblar la demostración.

## Corrección aplicada al header, navegación y catálogo

Esta sección actualiza el estado histórico de la tabla anterior. Las referencias revisadas fueron layouts.app/navigation-menu, layouts.profesor, layouts.appuserpanel, layouts.appuser, layouts.base/includes.navbar, dashboard, profesor/dashboard/index, users/panel/inicio, users/inicio y users/partials/course-card.

| Elemento | Corrección comprobada |
|---|---|
| Marca | Archivos originales inudiwhite.png, inudiblack.png e inudi_white.webp; logo en acceso, cabeceras y lateral de los perfiles. No se generó una marca alternativa. |
| Administración | Navegación superior: Inicio, Cursos, Usuarios, Pagos, Reseñas, Profesores, Ver como y Sesiones. Sin sidebar administrativo inventado. |
| Docente | Cabecera verde y lateral con Inicio, Mis cursos, Mi perfil y Salir. Sin Seguimiento ni Gestión de cursos como ítems del lateral. Inicio con Menú principal, Cursos asignados y acciones Ver curso/Gestionar contenido. |
| Estudiante | Lateral: Inicio, Mi perfil, Cursos Inscritos, Lista de deseos, Mis comentarios, Carritos, Ajustes y Salir. Lista de deseos y Ajustes conservan el destino # de la referencia, no se presentan como funcionalidades implementadas. |
| Catálogo | Navegación institucional azul con grupos Nosotros/Programas/Eventos/Editorial; contenido InudiClass, rutas académicas, búsqueda, categorías, nivel, vista de acceso y tarjetas. Las frases de esta pantalla proceden de users/inicio, no de una propuesta nueva. |
| Entrada | La raíz redirige al acceso. Se retiró la portada nueva del recorrido. El inicio de sesión dirige al panel correspondiente al rol. |
| Certificados | Se retiraron los mappings /verificar. Consulta en /cert y /cert/{code}, restringida a estudiantes. QR apunta a ese recorrido autenticado; no se afirma un servicio público de validación. |
| Ver como | Listado y consulta de perfiles en solo lectura. No se cambia la identidad del administrador ni se permite actuar por otra persona. |
| Sesiones | Registro real de acceso/salida y vigencia, búsqueda y filtro; no se reutiliza la auditoría de operaciones como si fuera historial de sesiones. Migración V3 preserva los datos previos. |
| Comentarios | Mis comentarios y Reseñas consultan comentarios de contenidos, excluyendo los aportes del tipo FORO; no se confunden con la reseña general de un curso. |

### Verificación y diferencias que siguen abiertas

55 pruebas satisfactorias, incluyendo renderizado JSP real, permisos de las rutas originales, presencia del logo, ausencia de los ítems/frases inventados, filtros del catálogo y sesión/consulta de perfiles. Evidencias visuales en docs/evidencias/menu-docente-corregido.jpg, menu-estudiante-corregido.jpg, menu-admin-corregido.jpg y catalogo-corregido.jpg. Se verificó en el navegador el acceso mediante 303 y el panel correspondiente, además de la navegación superior del administrador visible en la ventana de revisión.

La réplica no está terminada. Continúan abiertos:

- Formularios y tablas detallados de cursos, módulos, contenidos, usuarios y profesores: no se certifica equivalencia de todos sus campos, botones, filtros y paginación.
- Fotos de perfil, imágenes de cursos, estados adicionales y calificación individual de reseñas. Los cursos de demostración usan el logo de respaldo; no se importaron datos o fotografías personales.
- Panel del estudiante: falta siguiente contenido y el sistema de recompensas del original. Los indicadores nuevos retirados no se reemplazan por cifras de estrellas inventadas.
- Catálogo: falta el drawer exacto de filtros, iconografía de rutas, megamenús completos y footer institucional. Las rutas institucionales informativas todavía no tienen su implementación local; conservar enlaces no demuestra esas pantallas.
- Rutas con slug y formularios originales: se mantienen alias numéricos por compatibilidad local, no se afirma correspondencia completa.
- Comentarios/reseñas: falta reproducir puntuación del autor, respuestas y moderación en los listados originales.
- Sesiones: falta IP, ordenamiento y paginación del listado de referencia; el vencimiento muestra estado terminado, pero no se acredita listener de cierre por timeout.
- Pago PayPal/Culqi y boleta, Google Drive y envío del certificado por correo. La solicitud administrativa y almacenamiento local existentes no se consideran equivalentes.
- Reglas de progreso y emisión: persisten condiciones añadidas que deben compararse y corregirse contra la referencia.

No se modificó INFORME.md, no se eliminaron datos del entorno y no se publicaron commits.

## Unificación de cabecera solicitada por el usuario

El usuario autorizó posteriormente una navegación distinta de la referencia: cabecera única en páginas públicas y en los tres perfiles, con logo/nombre de Inudi Perú a la izquierda y cuenta/desplegable a la derecha. Esta decisión reemplaza las cabeceras por contexto descritas en el registro anterior; no se considera un desvío no autorizado.

- La cabecera compartida no contiene enlaces de navegación. Para visitantes presenta Iniciar Sesión; para usuarios autenticados presenta su nombre, Perfil/Mi perfil y Cerrar Sesión.
- Los enlaces administrativos están únicamente en el sidebar, en el orden Inicio, Cursos, Usuarios, Pagos, Reseñas, Profesores, Ver como y Sesiones.
- Estudiante y docente conservan sus enlaces por perfil y los accesos generales antes ubicados en la cabecera se presentan en el lateral. No se amplían permisos.
- En celular, la hamburguesa abre el lateral con el nombre del usuario y su desplegable. Hay cierre con el botón, fondo o Escape; el foco se mantiene dentro del menú abierto.
- El catálogo se sirve en `/`. `/cursos` y `/catalogo` redirigen a la raíz conservando la consulta; los detalles `/cursos/{id}` siguen disponibles. Búsqueda, filtros y accesos al catálogo generan la ruta raíz.
- El acceso también utiliza el fragmento común de cabecera; se conserva el formulario centrado.

Verificación: 57 pruebas automatizadas satisfactorias, sin fallos ni errores. La revisión visual comprobó el header de escritorio y el menú administrativo móvil de 390 × 844 con el nombre de usuario y desplegable abiertos; el sidebar reaparece al pasar a escritorio. Evidencias: `docs/evidencias/header-unificado-admin.jpg` y `docs/evidencias/header-unificado-movil.jpg`. Las demás diferencias funcionales registradas siguen abiertas.

## Ajustes UI/UX y cuenta móvil inferior

Se aplicó la nueva solicitud del usuario: cuenta móvil al pie del sidebar con navegación desplazable independiente y desplegable hacia arriba. El pie tiene posición sticky/bottom y permanece separado del área de enlaces. Se comprobó que su coordenada inferior no cambia al desplazar el menú.

- Estado activo azul tenue con indicador lateral; hover gris discreto. Se eliminó el estado verde y el contorno grueso del lateral, conservando foco visible para teclado.
- Radio normalizado a 2 px en superficies, inputs, botones y etiquetas; avatares circulares conservados.
- Etiquetas y campos con anchos coherentes, columnas que responden al espacio del contenido mediante container queries y botones que permiten envolver texto y acciones.
- Controles principales con altura mínima de 44 px; campos móviles de 16 px. Checkbox/radio mantienen su tamaño propio y etiquetas asociadas.
- Tablas con desplazamiento horizontal localizado, región accesible al teclado y ancho mínimo legible. No se fuerza a comprimir estados palabra por palabra.
- Selección de docentes mediante casillas vinculadas al mismo campo teacherIds, en lugar de un select múltiple dependiente de Ctrl.
- Corrección de bordes laterales no deseados derivados de utilidades sin preflight; sombras reducidas.

58 pruebas automatizadas satisfactorias. Se realizaron 31 mediciones de interfaz en muestras públicas/administración/estudiante/docente, a anchos de 320, 390, 768 y 1280: no se detectaron controles visibles principales fuera del área de contenido, ni desbordamiento horizontal de página, ni alturas inferiores al mínimo entre los controles medidos. Las tablas pueden desplazar su contenido dentro de su región. No son pruebas de todos los navegadores, teléfonos físicos ni de todas las combinaciones de datos. Evidencias: sidebar-cuenta-inferior-movil.jpg, formulario-cursos-movil.jpg y ui-normalizada-escritorio.jpg en docs/evidencias.

### Estado real del desarrollo

La aplicación local funciona y este ajuste visual está aplicado; el sistema completo todavía no está terminado como réplica. Este fue el estado previo a la siguiente aclaración de alcance. Las exclusiones y correcciones de la sección siguiente reemplazan sus pendientes de integraciones, reglas de progreso, siguiente contenido y recompensas. El despliegue público y la validación integral de equivalencia no están acreditados. No se interpreta una prueba visual satisfactoria como aprobación académica o cierre del desarrollo.

## Alcance actualizado: pagos manuales, reglas académicas y modales

El usuario confirmó que perfiles y reseñas se completarán posteriormente; pasarelas de pago, Google Drive y correo no se utilizarán. El pago se guarda como registro administrativo desde su listado y los archivos se almacenan por el sistema. Estas exclusiones reemplazan los pendientes históricos de integración; no deben volver a presentarse como funciones necesarias para terminar este alcance.

### Reglas cotejadas y corregidas

- El registro de progreso exige estudiante y matrícula vigentes, pero no una entrega, publicación ni examen aprobado. Se retiraron esas barreras añadidas. La finalización sigue siendo única por participante/contenido.
- Evaluaciones activas/inactivas, máximo de 1–20 intentos o 0 para ilimitados y umbral de aprobación de 0–100. Un examen inactivo no consume intentos. Se comprobó un intento número 21 en modo ilimitado.
- Siguiente contenido calculado por orden de módulo/contenido y finalizaciones reales; estrellas acumuladas de los intentos registrados, con umbrales de 50/70/90 %. No se crean cifras de recompensa ficticias.
- Certificación con matrícula vigente, curso habilitado y todos los contenidos completados; emisión idempotente. Se conserva el 100 % real y no se copia un redondeo prematuro del porcentaje.
- Entregas, calificación 0–20, corrección ponderada, permisos por curso y conservación de historia mantienen sus comprobaciones. La edición de cuestionarios con intentos conserva respuestas y umbral históricos.
- Indicadores administrativos de pagos calculados con operaciones CONFIRMADAS, no con importes pendientes/anulados.

Referencias internas consultadas: ProgresoController, CourseAccessService, CertificadoController, FormativeExamAttemptController y Profesor/FormativeExamController. No se ejecutaron integraciones ni se importaron datos personales.

### Creación y edición en la misma pantalla

Los botones de cursos, usuarios/profesores, módulos, contenidos, preguntas, pagos y calificaciones abren un modal común. También se usan modales para entregas y creación/edición/respuestas de aportes. Los perfiles y reseñas no se ampliaron en este incremento. Resolver una evaluación permanece en la lección, ya que no es un alta/edición de un registro administrativo.

El modal obtiene HTML de formularios MVC o de plantillas locales, conserva CSRF y validación de servidor, presenta errores sin borrar los campos y vuelve al listado/aula después de guardar. Permite cancelar, cerrar con Escape y retornar el foco al botón original. Las rutas de formulario se mantienen como alternativa; no se añadió API REST. El listado de pagos ya no contiene el acordeón de registro.

El registro manual permite confirmar un pago y activar matrícula en una sola transacción. La edición actualiza importe, referencia y vigencia sin duplicar matrícula. Conserva estudiante/curso y el estado de pagos ya confirmados o anulados para proteger el historial; permite confirmar/anular uno pendiente. La versión del registro evita sobrescribir una edición concurrente. V4 añade fecha de vigencia nullable sin inventar fechas de pagos históricos; V5 actualiza restricciones de evaluación sin recrear la base.

### Verificación y estado actual

71 pruebas automatizadas satisfactorias, sin fallos ni errores. La navegación real comprobó apertura de los formularios y edición de un pago de demostración en el mismo listado. Un cambio de estado inválido mostró su explicación y conservó los campos. El modal se revisó en escritorio y 390 × 844, sin desbordamiento horizontal en esa muestra. Evidencias: `docs/evidencias/modal-registrar-pago.jpg` y `docs/evidencias/modal-pago-movil.jpg`.

Este incremento completa las correcciones enumeradas, no certifica toda la réplica. Perfiles/reseñas quedan diferidos; fotos/imágenes propias, detalles de filtros/paginación, páginas institucionales y datos/cierre por timeout de sesiones mantienen su revisión específica. Pasarelas, Drive y correo están fuera de alcance. Se conserva la base local y no se modificó INFORME.md ni se publicaron commits.
