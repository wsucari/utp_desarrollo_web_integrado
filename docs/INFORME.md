# CARÁTULA

FACULTAD DE INGENIERÍA  
CARRERA PROFESIONAL DE INGENIERÍA DE SISTEMAS E INFORMÁTICA

**Sistema Web Integrado para la Gestión Académica de Inudi Perú**

Proyecto Final — Avance 3  
Curso: Desarrollo Web Integrado (100000ST62)  
Ciclo académico: 2026-2

Presentado por: Wilson Gregorio Sucari Turpo  
Docente: MBA Mg. Ing. Rene Alonso Nieto Valencia

Puno, Perú  
2026

# ÍNDICE

RESUMEN  
ABSTRACT

CAPÍTULO 1 ASPECTOS GENERALES  
1.1 Definición del problema  
1.2 Definición de objetivos del proyecto  
1.3 Equipo de trabajo  
1.4 Recursos y presupuesto  
1.5 Cronograma de actividades  
1.6 Cronograma de hitos del proyecto

CAPÍTULO 2 MARCO TEÓRICO  
2.1 Fundamento teórico

CAPÍTULO 3 DESARROLLO DE LA SOLUCIÓN  
3.1 Análisis del sistema  
3.2 Diseño del sistema  
3.3 Arquitectura del sistema  
3.4 Seguridad del sistema  
3.5 Pruebas y validación  
3.6 Gestión de versionamiento con Git y GitHub

CONCLUSIONES  
RECOMENDACIONES  
BIBLIOGRAFÍA  
ANEXOS  
ENCUESTA

# ÍNDICE DE FIGURAS

1. Diagrama de casos de uso.
2. Interfaz de gestión y formulario modal.
3. Modelo entidad-relación.
4. Arquitectura de integración Angular–Spring Boot.
5. Resultados de pruebas automatizadas.

# ÍNDICE DE TABLAS

1. Alternativas de solución.
2. Recursos y presupuesto.
3. Cronograma y distribución temporal.
4. Estructura de desglose del trabajo.
5. Hitos del proyecto.
6. Requisitos funcionales.
7. Requisitos no funcionales.
8. Matriz de trazabilidad.
9. Especificaciones de casos de uso.
10. Diccionario de datos.
11. Componentes y dependencias.
12. Contratos principales de la API.
13. Permisos por perfil.
14. Resultados de pruebas.
15. Instrumento de valoración de uso.

# RESUMEN

El proyecto desarrolla el Sistema Web Integrado para la Gestión Académica de Inudi Perú, orientado a relacionar la administración de cursos y usuarios con las matrículas y las evidencias del aprendizaje. La solución diferencia el pago administrativo, la vigencia del acceso, el progreso y los resultados de evaluación para mantener información consistente durante el recorrido del estudiante.

La interfaz utiliza Angular y se comunica mediante una API REST con un backend desarrollado en Java y Spring Boot. La persistencia se implementa con Spring Data JPA, Hibernate y una base relacional H2. Spring Security controla la autenticación por sesiones y los permisos sobre las operaciones y los recursos. Los módulos permiten organizar contenidos, registrar pagos, habilitar matrículas, recibir actividades, calificar entregas, resolver evaluaciones, participar en foros y descargar certificados digitales.

La validación comprendió 85 pruebas Java, 15 pruebas Angular y cinco recorridos completos de navegador, todos satisfactorios. Se comprobaron reglas académicas, persistencia, validaciones, seguridad e integración. Los resultados respaldan los escenarios evaluados en un entorno local con datos de demostración; no constituyen una medición de impacto institucional ni una validación de operación productiva.

**Palabras clave:** gestión académica, Angular, Spring Boot, API REST, persistencia, seguridad.

# ABSTRACT

This project develops the Integrated Web System for Academic Management at Inudi Perú, connecting course and user administration with enrollment and learning evidence. The solution distinguishes administrative payment records, access validity, progress and assessment results to maintain consistent information throughout each student's learning process.

The Angular interface communicates through a REST API with a backend developed in Java and Spring Boot. Persistence uses Spring Data JPA, Hibernate and an H2 relational database. Spring Security provides session-based authentication and authorization for operations and resources. The modules support learning content, payment records, enrollment activation, assignment submissions, grading, formative assessments, discussion forums and downloadable digital certificates.

Validation included 85 Java tests, 15 Angular tests and five complete browser workflows, all of which passed. The checks covered academic rules, persistence, validation, security and integration. These results support the tested scenarios in a local environment with demonstration data; they do not establish institutional impact or readiness for production use.

**Keywords:** academic management, Angular, Spring Boot, REST API, persistence, security.

# CAPÍTULO 1 ASPECTOS GENERALES

El proyecto reúne las operaciones que acompañan al estudiante desde la selección de un curso hasta su culminación. Su desarrollo relaciona procesos, requisitos, datos, componentes y pruebas, de manera que cada función tenga una finalidad identificable dentro de la gestión académica.

## 1.1 DEFINICIÓN DEL PROBLEMA

La gestión académica requiere mantener coherencia entre usuarios, cursos, condiciones de acceso y evidencias del aprendizaje. Un pago, una matrícula, una entrega y una calificación representan hechos diferentes, aunque pertenezcan al mismo participante.

El problema abordado consiste en controlar estas relaciones mediante reglas uniformes. Una solicitud pendiente no debe habilitar el aula; una entrega debe vincularse con su autor y actividad; y un certificado debe corresponder a condiciones verificables. El sistema organiza estos controles sin atribuir a la institución pérdidas económicas o indicadores de servicio que no han sido medidos.

### 1.1.1 DESCRIPCIÓN DEL PROBLEMA

El Instituto Universitario de Innovación Ciencia y Tecnología Inudi Perú desarrolla formación continua mediante cursos, diplomados, especializaciones y programas de posdoctorado. Su misión se orienta a la capacitación profesional, la producción intelectual, la innovación y la transferencia tecnológica. Su visión plantea reconocimiento en los países de habla hispana. La institución registra su dirección de contacto en Jayllihuaya, Puno (Inudi Perú, s. f.). [Información institucional](https://inudi.edu.pe/nosotros/).

El proyecto atiende la gestión de cursos y distingue tres responsabilidades: el administrador organiza cuentas, cursos y acceso; el profesor desarrolla contenidos y califica actividades de sus cursos asignados; y el estudiante consulta recursos, participa y revisa resultados.

El análisis identifica la necesidad de separar pago y matrícula. El primero conserva el antecedente administrativo; la segunda determina la inscripción y su vigencia. También distingue progreso y desempeño: completar contenidos expresa un recorrido registrado, mientras que las notas y evaluaciones conservan resultados específicos. Estas diferencias permiten interpretar la situación académica sin depender de un indicador aislado.

### 1.1.2 DIAGRAMAS DE PROCESOS DE NEGOCIO

El modelo de procesos se organiza en cuatro recorridos relacionados. Sus responsables y decisiones se describen para representar el funcionamiento atendido por la aplicación, sin presuponer procedimientos institucionales no documentados.

**Preparación del curso.** El administrador registra los datos generales, asigna profesores y determina la publicación. El profesor autorizado o el administrador organiza módulos, contenidos y preguntas. Los datos inválidos se rechazan antes de guardar.

**Solicitud y habilitación de matrícula.** El estudiante selecciona cursos y registra una solicitud pendiente. La administración valida o registra el pago y establece la vigencia. Su confirmación crea o actualiza la matrícula dentro de la misma transacción. El procedimiento no procesa cobros bancarios.

**Aprendizaje y seguimiento.** El estudiante con acceso vigente consulta contenidos, registra su finalización, entrega actividades y participa en evaluaciones y foros. El profesor revisa las entregas y proporciona calificaciones y retroalimentación. El progreso se calcula independientemente de la aprobación de evaluaciones o la participación.

**Certificación.** La emisión comprueba matrícula vigente, certificación habilitada y finalización de todos los contenidos. Una solicitud repetida recupera el certificado existente. La descarga está protegida y la consulta del código requiere sesión de estudiante.

Estos recorridos se relacionan con los actores y casos de uso del apartado 3.2.1. Su representación conjunta evita repetir diagramas para cada pantalla.

### 1.1.3 ALTERNATIVAS DE SOLUCIÓN Y SOLUCIÓN SELECCIONADA

La selección consideró la capacidad de cada alternativa para relacionar los registros y aplicar las reglas del proyecto. La comparación es de diseño y no constituye una evaluación comercial de productos.

**Tabla 1. Alternativas de solución**

| Alternativa | Aporte | Consideración |
|---|---|---|
| Herramientas independientes de registro | Organizan información específica de pagos, usuarios o actividades. | Requieren controles adicionales para relacionar datos y permisos. |
| Plataforma educativa configurable | Ofrece funciones generales de cursos y aprendizaje. | Su adecuación requiere contrastar configuración y reglas particulares. |
| Aplicación web integrada | Permite definir datos, servicios y controles ajustados al alcance. | Exige desarrollar y verificar todos los componentes involucrados. |

Se seleccionó una aplicación integrada con Angular y servicios REST en Spring Boot. Esta organización permite mantener las reglas en el servidor y vincular los requisitos con operaciones comprobables. La interfaz facilita la interacción; los servicios validan y procesan las solicitudes; y la persistencia conserva sus relaciones.

## 1.2 DEFINICIÓN DE OBJETIVOS DEL PROYECTO

Los objetivos vinculan las necesidades de gestión con resultados verificables de análisis, implementación e integración.

### 1.2.1 OBJETIVO GENERAL

Desarrollar un sistema web integrado para la gestión académica de Inudi Perú mediante Angular, una API REST con Java y Spring Boot y una base de datos relacional, que permita administrar cursos, matrículas y evidencias de aprendizaje con información consistente y acceso controlado.

### 1.2.2 OBJETIVOS ESPECÍFICOS

1. Identificar actores, procesos, requisitos y reglas de la gestión académica.
2. Diseñar una arquitectura por capas y un modelo relacional coherentes con los procesos.
3. Integrar componentes, rutas y formularios Angular con servicios REST y manejo de errores.
4. Implementar cursos, asignaciones docentes, pagos y matrículas con vigencia e historial.
5. Incorporar contenidos, progreso, actividades, evaluaciones y participación académica.
6. Proteger operaciones y archivos mediante autenticación, autorización y validaciones.
7. Emitir certificados bajo condiciones comprobables y sin duplicaciones.
8. Verificar los componentes y recorridos mediante pruebas unitarias, de integración y funcionales.

### 1.2.3 ALCANCES Y LIMITACIONES

El alcance corresponde a una aplicación local integrada. Se diferencia entre funciones disponibles, exclusiones deliberadas y condiciones todavía no acreditadas.

#### 1.2.3.1 Dentro del alcance

Se incluyen registro y administración de usuarios; perfiles básicos; catálogo y gestión de cursos; asignación docente; módulos y seis tipos de contenido; carrito y solicitudes; pagos manuales y matrículas; progreso; entregas y calificaciones; evaluaciones de cinco tipos; foros y reseñas básicas; certificados; seguimiento y exportación CSV; consulta de operaciones, sesiones y vista administrativa de perfiles.

Los mantenimientos utilizan modales y conservan los datos ante errores. La interfaz incorpora validaciones, estados de carga, resultados vacíos, mensajes de confirmación y recuperación de consultas fallidas.

#### 1.2.3.2 Fuera del alcance

Se excluyen pasarelas bancarias, integración financiera institucional, Google Drive, envío de correo, administración de editorial y eventos, aplicaciones móviles nativas, autenticación social, doble factor e importación de datos personales productivos. Tampoco se implementa un servicio público independiente de validación de certificados ni una plataforma propia de transmisión de video.

#### 1.2.3.3 Limitaciones

La ejecución y las pruebas utilizan H2 y archivos locales. No se acreditan despliegue público, carga institucional masiva, disponibilidad continua o compatibilidad con otros motores. La revisión de interfaz comprende Chromium y muestras de escritorio y móvil.

La ampliación de perfiles y reseñas no está completada. Permanecen aspectos de imágenes propias, filtros y paginación, páginas institucionales y detalle de cierre de sesiones por inactividad. No se ha aplicado una evaluación de aceptación institucional. Los certificados de demostración se identifican como documentos de prueba.

### 1.2.4 JUSTIFICACIÓN

El sistema permite consultar la situación académica mediante registros relacionados. La administración distingue solicitudes y matrículas; el profesor identifica actividades y autores; y el estudiante consulta su recorrido y resultados.

La separación entre interfaz, servicios y persistencia facilita revisar responsabilidades y evita que las reglas dependan exclusivamente del navegador. Desde la formación académica, el proyecto conecta análisis, diseño, programación y pruebas mediante una solución ejecutable. Su aporte se sustenta en organización y control funcional, sin atribuir mejoras cuantitativas de tiempo, costos o satisfacción no medidas.

## 1.3 EQUIPO DE TRABAJO

El proyecto está a cargo de Wilson Gregorio Sucari Turpo, quien asume análisis, diseño, desarrollo, pruebas y documentación. La organización individual mantiene diferenciados los productos de cada actividad: requisitos, modelos, código y evidencias.

La revisión combina comprobaciones automatizadas y recorridos de navegador. Estos mecanismos apoyan el control técnico, pero no sustituyen la aceptación de usuarios institucionales.

## 1.4 RECURSOS Y PRESUPUESTO

El presupuesto representa el desembolso adicional previsto para una demostración local con recursos disponibles. No equivale a una valorización económica completa.

**Tabla 2. Recursos y presupuesto**

| Recurso | Uso | Desembolso adicional previsto |
|---|---|---:|
| Equipo y conectividad disponibles | Desarrollo y ejecución local. | S/ 0,00 |
| Java, Spring Boot y Maven | Backend, pruebas y empaquetado. | S/ 0,00 |
| Angular, TypeScript, Node.js y npm | Interfaz y compilación. | S/ 0,00 |
| H2, JPA/Hibernate y Flyway | Persistencia y esquema. | S/ 0,00 |
| Tailwind CSS y herramientas de prueba | Presentación y validación. | S/ 0,00 |
| Git, GitHub y documentación | Versionamiento y organización. | S/ 0,00 |
| **Total previsto** | | **S/ 0,00** |

No se cuantifican dedicación personal, depreciación, consumo o mantenimiento, porque no existe un registro validado de horas y tarifas. Tampoco se incluye infraestructura productiva.

## 1.5 CRONOGRAMA DE ACTIVIDADES

La planificación se expresa en dieciocho semanas académicas. Los periodos indican distribución prevista y no acreditan fechas exactas de ejecución.

**Tabla 3. Cronograma y distribución temporal**

| Actividad | S. 1–3 | S. 4–5 | S. 6–7 | S. 8–10 | S. 11–13 | S. 14–15 | S. 16–18 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Contexto y necesidades | X | X | | | | | |
| Requisitos y diseño | | X | X | | | | |
| Backend y persistencia | | | X | X | X | | |
| Angular y servicios REST | | | | | X | X | |
| Integración y pruebas | | | | X | X | X | X |
| Documentación y evidencias | | X | X | X | X | X | X |
| Presentación y cierre | | | | | | | X |

### 1.5.1 DIAGRAMA DE GANTT

La tabla 3 funciona como representación temporal del Gantt: cada marca identifica un periodo de trabajo. La superposición de pruebas y documentación refleja su relación continua con el desarrollo. Los bloques de integración dependen de disponer de datos, servicios y pantallas ejecutables.

### 1.5.2 ESTRUCTURA DE DESGLOSE DEL TRABAJO — WBS/EDT

La EDT organiza el trabajo en productos revisables, evitando confundir actividades técnicas con funcionalidades del usuario.

**Tabla 4. Estructura de desglose del trabajo**

| Código | Producto | Componentes |
|---|---|---|
| 1 | Análisis | Procesos, objetivos, requisitos y reglas. |
| 2 | Diseño | Casos de uso, interfaces, clases y datos. |
| 3 | Backend | Servicios, persistencia, seguridad y API. |
| 4 | Frontend | Componentes, rutas, formularios e interacción. |
| 5 | Integración | Sesiones, errores, archivos y contratos. |
| 6 | Validación | Pruebas y recorridos por perfil. |
| 7 | Documentación | Informe, referencias y evidencias. |
| 8 | Presentación | Demostración y exposición. |

## 1.6 CRONOGRAMA DE HITOS DEL PROYECTO

Los hitos establecen criterios de revisión y no sustituyen la evidencia de ejecución.

**Tabla 5. Hitos del proyecto**

| Semana | Hito | Criterio |
|---|---|---|
| 5 | Definición | Problema, objetivos y alcance relacionados. |
| 7 | Diseño | Requisitos, casos de uso y datos coherentes. |
| 10 | Backend | Operaciones y persistencia verificables. |
| 13 | Interfaz | Componentes y contratos disponibles. |
| 15 | Integración | Flujos por perfil y pruebas registrados. |
| 17 | Consolidación | Correspondencia entre sistema e informe. |
| 18 | Presentación | Demostración y documentación organizadas. |

# CAPÍTULO 2 MARCO TEÓRICO

## 2.1 FUNDAMENTO TEÓRICO

**Gestión académica integrada.** En el proyecto, la integración consiste en conservar relaciones entre administración, acceso y aprendizaje. El modelo distingue hechos administrativos y académicos para que cada operación tenga datos, responsables y condiciones identificables.

**Componentes y formularios Angular.** La interfaz se divide en componentes con responsabilidades delimitadas. Los formularios reactivos representan los campos y validaciones mediante un modelo explícito, lo que facilita consultar su estado y conservar los valores durante la interacción (Angular, s. f.-c). [Formularios reactivos](https://angular.dev/guide/forms/reactive-forms).

**Comunicación HTTP.** HttpClient proporciona mecanismos para enviar solicitudes y recibir respuestas. En la aplicación, los servicios Angular centralizan este intercambio y sus modelos TypeScript describen los datos esperados (Angular, s. f.-b). [Cliente HTTP](https://angular.dev/guide/http).

**Servicios REST y Spring MVC.** Los controladores reciben solicitudes HTTP y devuelven representaciones de los recursos. Spring permite escribir el cuerpo de la respuesta mediante conversión de mensajes; esta capacidad sustenta los contratos JSON del sistema (Spring, s. f.-b). [Respuesta de controladores](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html).

**Organización por capas.** La separación de paquetes facilita localizar responsabilidades y dependencias. La documentación de Spring Boot orienta la ubicación de la clase principal y de los componentes dentro de una estructura reconocible (Spring, s. f.-c). [Organización del código](https://docs.spring.io/spring-boot/3.5/reference/using/structuring-your-code.html).

**Persistencia y transacciones.** Hibernate relaciona entidades y asociaciones de objetos con estructuras relacionales (Hibernate, s. f.). [Guía de Hibernate ORM](https://docs.hibernate.org/orm/6.6/userguide/html_single/). Las transacciones delimitan operaciones que deben mantenerse consistentes; los servicios pueden coordinar varios repositorios dentro de una misma unidad de trabajo (Spring, s. f.-d). [Transacciones con Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html).

**Base de datos.** H2 dispone de modalidades de almacenamiento en archivo y en memoria (H2 Database Engine, s. f.). [Características de H2](https://www.h2database.com/html/features.html). El proyecto utiliza archivo para la ejecución habitual y bases en memoria para aislar las pruebas.

**Autorización y CSRF.** Los guards de Angular orientan el acceso a las rutas, pero los controles del servidor siguen siendo necesarios (Angular, s. f.-a). [Protección de rutas](https://angular.dev/guide/routing/route-guards). La protección CSRF comprueba las solicitudes que modifican información dentro de una aplicación autenticada mediante sesión (Spring, s. f.-a). [Protección CSRF](https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html).

**Desarrollo guiado por pruebas.** TDD organiza el trabajo mediante una prueba que falla, implementación suficiente y refactorización. Su finalidad es orientar el desarrollo con comportamientos verificables, no únicamente acumular pruebas al finalizar (Fowler, 2023). [Desarrollo guiado por pruebas](https://martinfowler.com/bliki/TestDrivenDevelopment.html).

# CAPÍTULO 3 DESARROLLO DE LA SOLUCIÓN

## 3.1 ANÁLISIS DEL SISTEMA

El análisis organiza las funciones por actor y establece condiciones comprobables. Los identificadores permiten relacionar requisitos, casos de uso y pruebas sin repetir toda su descripción.

### 3.1.1 REQUISITOS FUNCIONALES

Los requisitos tienen versión documental 1.0 y proceden del análisis de procesos, las decisiones de alcance y el código revisado. Son de prioridad alta dentro del alcance, salvo las funciones básicas de perfil y reseñas, cuya ampliación está delimitada. Su implementación se comprobó mediante los escenarios descritos en 3.5; esta condición no expresa cobertura exhaustiva.

**Tabla 6. Requisitos funcionales**

| Código | Requisito verificable | Actor o condición |
|---|---|---|
| RF-01 | Iniciar y cerrar sesión, identificando al usuario y su rol. | Cuenta activa. |
| RF-02 | Registrar estudiantes y administrar usuarios y profesores. | Registro público o administración autorizada. |
| RF-03 | Actualizar el perfil básico y cambiar contraseña validando la actual. | Titular autenticado. |
| RF-04 | Consultar cursos publicados mediante búsqueda y filtros. | Visitante o usuario. |
| RF-05 | Crear, editar, publicar, ocultar y eliminar cursos vacíos; asignar profesores. | Administrador. |
| RF-06 | Crear, editar y ordenar módulos y contenidos. | Administrador o profesor asignado. |
| RF-07 | Consultar los seis tipos de contenido y sus recursos autorizados. | Acceso vigente o vista previa permitida. |
| RF-08 | Seleccionar cursos y registrar solicitudes pendientes. | Estudiante. |
| RF-09 | Registrar y editar pagos, actualizando la matrícula al confirmar. | Administrador; control de versión. |
| RF-10 | Consultar vigencia y cancelar matrículas conservando historial. | Administración y controles de acceso. |
| RF-11 | Registrar finalización única por contenido y calcular progreso. | Estudiante con matrícula vigente. |
| RF-12 | Recibir archivos de actividades y controlar intentos de entrega. | Estudiante autorizado. |
| RF-13 | Registrar notas de cero a veinte y retroalimentación. | Gestor del curso. |
| RF-14 | Configurar preguntas de cinco tipos y sus puntajes. | Gestor; restricciones por historial. |
| RF-15 | Resolver evaluaciones con corrección ponderada y límites configurables. | Estudiante; evaluación activa. |
| RF-16 | Consultar resultados y explicaciones de intentos autorizados. | Titular o gestor del curso. |
| RF-17 | Publicar, responder, editar y retirar aportes. | Usuario autorizado; control de autoría. |
| RF-18 | Reaccionar, valorar aportes ajenos y registrar reseñas básicas. | Permisos y rangos válidos. |
| RF-19 | Emitir y descargar un certificado único; consultar su código. | Condiciones académicas cumplidas. |
| RF-20 | Consultar seguimiento, exportar CSV y revisar operaciones administrativas. | Gestor o administrador, según operación. |
| RF-21 | Crear y editar mediante modales, conservando campos ante errores. | Formularios de mantenimiento. |
| RF-22 | Mostrar siguiente contenido y estrellas de intentos registrados. | Historial real del estudiante. |
| RF-23 | Consumir la API desde Angular y presentar carga, vacío y errores. | Contratos HTTP y validaciones. |
| RF-24 | Consultar registros de acceso y filtrar sesiones. | Administrador. |
| RF-25 | Consultar perfiles en vista administrativa de solo lectura. | Se conserva la identidad del administrador. |

Las dependencias funcionales siguen la secuencia identidad–curso–matrícula–contenido–evidencia. Por ejemplo, RF-11 requiere RF-06 y RF-10; RF-13 depende de RF-12; y RF-19 utiliza la matrícula y las finalizaciones de RF-11.

### 3.1.2 REQUISITOS NO FUNCIONALES

Los criterios se formulan como condiciones observables. No se establecen garantías de rendimiento o disponibilidad sin medición.

**Tabla 7. Requisitos no funcionales**

| Código | Condición | Comprobación |
|---|---|---|
| RNF-01 | Persistencia sin recrear el esquema al iniciar. | H2 en archivo, Flyway y validación Hibernate. |
| RNF-02 | Contraseñas protegidas y escrituras con CSRF. | BCrypt y rechazos de acceso. |
| RNF-03 | Autorización por rol, titularidad y curso. | Respuestas 401/403 y servicios. |
| RNF-04 | Archivos privados de hasta 10 MB y formatos restringidos. | Validación de almacenamiento. |
| RNF-05 | Errores comprensibles sin trazas internas. | Contrato de errores y formularios. |
| RNF-06 | Interfaz en español y codificación UTF-8. | Pantallas y pruebas HTTP. |
| RNF-07 | Presentación, negocio y persistencia separados. | Organización de componentes. |
| RNF-08 | Pruebas aisladas de la base habitual. | H2 en memoria y archivos de prueba. |
| RNF-09 | Integridad ante intentos simultáneos. | Bloqueos, unicidad y concurrencia. |
| RNF-10 | Adaptación móvil y desplazamiento propio de tablas. | Muestras de escritorio y móvil. |

### 3.1.3 REGLAS DE NEGOCIO

Las reglas concretan las condiciones que aplican los servicios:

- **RN-01:** las operaciones de aprendizaje corresponden a estudiantes activos y con acceso vigente.
- **RN-02:** una solicitud pendiente no habilita matrícula; la confirmación del pago actualiza el acceso en la misma transacción.
- **RN-03:** el profesor gestiona únicamente cursos asignados.
- **RN-04:** el vencimiento no elimina pagos, progreso ni evidencias.
- **RN-05:** cada estudiante tiene una finalización única por contenido.
- **RN-06:** marcar un contenido no exige entregar, publicar ni aprobar.
- **RN-07:** las actividades admiten de uno a diez intentos; las evaluaciones, de uno a veinte o cero para intentos ilimitados.
- **RN-08:** un examen inactivo no admite nuevos intentos; su aprobación se configura entre cero y cien.
- **RN-09:** las notas de actividades están entre cero y veinte; la evaluación utiliza puntos ponderados.
- **RN-10:** las estrellas se asignan desde 50 %, 70 % y 90 % para una, dos y tres estrellas.
- **RN-11:** las respuestas de foro pertenecen al mismo contenido; retirar un aporte conserva las respuestas.
- **RN-12:** cada usuario mantiene una reacción y una valoración por publicación; no puede valorar su propio aporte.
- **RN-13:** las preguntas y el umbral de aprobación no se modifican cuando existe historial de intentos.
- **RN-14:** el certificado exige matrícula vigente, certificación habilitada y todos los contenidos completados; la emisión repetida recupera el mismo registro.
- **RN-15:** el historial académico impide eliminar físicamente cursos o contenidos relacionados.
- **RN-16:** debe conservarse al menos un administrador activo.
- **RN-17:** los materiales requieren acceso autorizado; entregas y resultados se limitan al titular y gestores permitidos.
- **RN-18:** editar un pago conserva estudiante y curso; los estados confirmados o anulados respetan sus restricciones y el registro utiliza control de versión.

### 3.1.4 MATRIZ DE TRAZABILIDAD

La matriz relaciona grupos funcionales con casos de uso y evidencias. Los requisitos transversales RF-21 y RF-23 se aplican a los formularios y comunicaciones correspondientes.

**Tabla 8. Matriz de trazabilidad**

| Requisitos | Casos de uso | Evidencia principal |
|---|---|---|
| RF-01–RF-03 | CU-01, CU-02 | Acceso, cuentas y perfil. |
| RF-04, RF-07 | CU-03 | Catálogo, filtros y permisos. |
| RF-05, RF-06, RF-14 | CU-04 | CRUD y protección del historial. |
| RF-08 | CU-05 | Solicitud pendiente sin acceso. |
| RF-09, RF-10 | CU-06 | Pago, vigencia y matrícula. |
| RF-11, RF-22 | CU-07 | Finalización y siguiente contenido. |
| RF-12, RF-13 | CU-08 | Archivo privado y calificación. |
| RF-15, RF-16 | CU-09 | Cinco tipos y resultado autorizado. |
| RF-17, RF-18 | CU-10 | Aportes, interacciones y reseñas básicas. |
| RF-19 | CU-11 | PDF, unicidad y permisos. |
| RF-20, RF-24, RF-25 | CU-12 | Seguimiento, sesiones y vista de perfiles. |
| RF-21, RF-23 | CU-01–CU-12, según operación | Modales, HTTP y recuperación de errores. |

## 3.2 DISEÑO DEL SISTEMA

El diseño relaciona las interacciones del usuario con componentes y datos. Las decisiones de interfaz mantienen el contexto; los modelos conservan asociaciones; y los servicios reúnen las reglas compartidas.

### 3.2.1 CASOS DE USO

Los actores son visitante, estudiante, profesor y administrador. Un mismo tipo de recurso puede ser consultado por distintos actores con permisos diferentes.

#### 3.2.1.1 Diagrama de casos de uso

El visitante consulta el catálogo y puede registrarse. El estudiante utiliza los servicios de inscripción y aprendizaje. El profesor gestiona sus cursos y revisa evidencias. El administrador mantiene los registros y realiza consultas de control. Las operaciones protegidas requieren autenticación y las condiciones específicas del recurso.

[[imagen 1: diagrama de casos de uso con visitante, estudiante, profesor y administrador, y sus funciones de consulta, gestión, aprendizaje y control]]

#### 3.2.1.2 Especificación de casos de uso

Las especificaciones resumen precondiciones, interacción, excepciones y resultado. Las reglas aplicables se identifican mediante los códigos del apartado 3.1.3.

**Tabla 9. Especificaciones de casos de uso**

| Caso y actor | Precondición | Flujo principal | Excepción y resultado |
|---|---|---|---|
| CU-01 Autenticarse. Usuario. | Cuenta activa. | Ingresa credenciales; el servidor valida; la interfaz abre el panel del rol. | Credenciales inválidas: error sin sesión. Salida: sesión invalidada. RN-01. |
| CU-02 Mantener identidad. Administrador o titular. | Permiso sobre la cuenta. | Abre formulario, modifica datos y confirma. | Datos inválidos se conservan; el perfil no eleva roles. Cuenta actualizada. RN-16. |
| CU-03 Consultar cursos. Visitante o usuario. | Publicación o permiso de gestión. | Filtra catálogo y abre detalle o recurso permitido. | Se informa vacío o acceso restringido. No se revelan archivos privados. RN-17. |
| CU-04 Gestionar curso. Administrador o profesor asignado. | Permiso sobre curso. | Mantiene datos, módulos, contenidos y preguntas mediante modales. | Se rechaza borrar historial o alterar cuestionarios protegidos. RN-03, RN-13, RN-15. |
| CU-05 Solicitar matrícula. Estudiante. | Cuenta activa y curso seleccionable. | Añade al carrito y confirma solicitud. | Solicitud pendiente no concede aula. Se conserva registro administrativo. RN-02. |
| CU-06 Administrar pago. Administrador. | Estudiante, curso y datos válidos. | Registra o edita pago; confirma y establece vigencia. | Error o conflicto impide actualización parcial. Matrícula consistente. RN-02, RN-18. |
| CU-07 Registrar progreso. Estudiante. | Matrícula vigente. | Consulta contenido y marca finalización. | Repetir no duplica; acceso vencido se rechaza. RN-04–RN-06. |
| CU-08 Entregar y calificar. Estudiante y gestor. | Actividad, acceso e intentos disponibles. | Estudiante adjunta; gestor descarga, califica y comenta. | Archivo o nota inválidos se rechazan; evidencia permanece privada. RN-07, RN-09, RN-17. |
| CU-09 Resolver evaluación. Estudiante. | Examen activo e intentos disponibles. | Responde, confirma y consulta resultado ponderado. | No se consumen intentos inválidos por examen inactivo; resultados ajenos restringidos. RN-07–RN-10. |
| CU-10 Participar. Usuario autorizado. | Acceso al contenido o curso. | Publica, responde, edita, reacciona o valora. | Se controla autoría y autovaloración; retiro conserva respuestas. RN-11, RN-12. |
| CU-11 Obtener certificado. Estudiante. | Condiciones de culminación cumplidas. | Solicita emisión, descarga PDF y consulta código. | Condición faltante bloquea emisión; repetición recupera registro. RN-14, RN-17. |
| CU-12 Consultar control. Gestor o administrador. | Permiso según consulta. | Revisa seguimiento, CSV, operaciones, sesiones o perfiles. | El docente no accede a funciones administrativas; vista previa mantiene identidad y solo lectura. RN-03, RN-17. |

### 3.2.2 PROTOTIPOS UX/UI

La interfaz organiza catálogo, paneles, listados, gestión del curso y aula. La cabecera mantiene la identidad institucional y la cuenta; el lateral reúne la navegación según el rol. En móviles, el menú se abre mediante un botón y la cuenta permanece al pie, con desplegable ascendente.

Los formularios de creación y edición se presentan sobre el listado o aula mediante modales. Los campos tienen etiquetas, validaciones y mensajes; los errores del servidor conservan los valores ingresados. Las tablas desplazan su contenido dentro de su propia región.

Las pantallas descritas tienen implementación funcional. Su comprobación no se interpreta como una evaluación completa de accesibilidad o aceptación.

[[imagen 2: interfaz Angular de gestión con cabecera, navegación lateral y formulario modal con campos y acciones]]

### 3.2.3 PROTOTIPOS DE INFORMES Y REPORTES

El seguimiento académico presenta participantes, vigencia, porcentaje de progreso, entregas, calificaciones e intentos de evaluación. La exportación CSV facilita revisar estos datos fuera de la pantalla y conserva el permiso del gestor.

Las consultas administrativas muestran pagos, matrículas, certificados, sesiones y operaciones registradas. El certificado PDF es un documento individual con titular, curso, código, fecha y QR. No se presentan predicciones ni indicadores económicos calculados a partir de pagos pendientes.

### 3.2.4 DISEÑO DE CLASES

Account representa la identidad y el rol. Course se relaciona con profesores y LearningModule; cada módulo agrupa Content. Enrollment representa el vínculo académico y Payment el antecedente administrativo.

Progress conserva finalizaciones; Submission relaciona estudiante, actividad y calificación. Question define preguntas; ExamAttempt y ExamAnswer conservan intentos y respuestas. Discussion organiza aportes y respuestas; Reaction, Rating y CourseReview registran interacciones. Certificate conserva la emisión, mientras AuditEntry y UserSession cumplen funciones de control.

AcademicService coordina reglas y operaciones. Los repositorios resuelven persistencia; StorageService administra archivos; ExamGrader corrige respuestas; y CertificatePdfService genera documentos. Los controladores adaptan solicitudes sin trasladar las reglas al navegador.

### 3.2.5 DISEÑO DE LA BASE DE DATOS

La base utiliza relaciones explícitas, restricciones y control de versiones. La información de los archivos se registra en tablas, mientras su contenido permanece en almacenamiento privado.

#### 3.2.5.1 Diseño lógico

La estructura principal es curso–módulo–contenido. Las cuentas se vinculan con cursos mediante matrícula y asignación docente. Pagos, progreso y evidencias conservan sus propias relaciones.

Un curso admite varios módulos y profesores; un contenido reúne finalizaciones, entregas, preguntas o publicaciones según su tipo. Cada intento conserva respuestas y resultados. El certificado es único por estudiante y curso.

[[imagen 3: modelo entidad-relación con usuarios, cursos, módulos, contenidos, matrículas, pagos, progreso, entregas, evaluaciones, participación y certificados]]

#### 3.2.5.2 Diseño físico

H2 mantiene diecinueve tablas de negocio, una tabla de sesiones y la tabla técnica de historial de Flyway. Las entidades de negocio utilizan identificadores BIGINT y versión optimista; la asignación docente utiliza clave compuesta. El registro de sesiones tiene su propia estructura.

Los importes se almacenan como NUMERIC(10,2). Las claves foráneas conservan referencias; las restricciones únicas evitan duplicar matrículas, finalizaciones y certificados. Las fechas y estados permiten calcular vigencia sin borrar antecedentes. Hibernate valida el esquema al iniciar.

#### 3.2.5.3 Diccionario de datos

El diccionario reúne campos y restricciones principales de todas las tablas funcionales. Los atributos de identidad y versión comunes se explican en el diseño físico.

**Tabla 10. Diccionario de datos**

| Tabla | Datos principales | Restricción o relación |
|---|---|---|
| accounts | full_name, email, password, role, active, professional_title, phone. | Correo único; contraseña como hash. |
| courses | title, description, objectives, category, level, duration_hours, price, published, certificate_enabled. | Precio no negativo; duración válida. |
| course_teachers | course_id, teacher_id. | Clave compuesta; cuenta docente activa. |
| learning_modules | course_id, title, position. | Pertenece a un curso. |
| contents | module_id, title, type, body, resource_url, file_key, file_name, preview, position, max_attempts, pass_percent, exam_active. | Tipo, orden, intentos y porcentaje válidos. |
| enrollments | student_id, course_id, starts_at, expires_at, status. | Única por estudiante y curso. |
| payments | student_id, course_id, amount, status, reference, created_at, confirmed_at, expires_at. | Importe y estado; versión concurrente. |
| cart_items | student_id, course_id. | Selección única por estudiante y curso. |
| progress | student_id, content_id, completed_at. | Finalización única por estudiante y contenido. |
| submissions | student_id, content_id, attempt_number, file_key, file_name, comment, grade, feedback, created_at. | Intento único y nota de 0 a 20. |
| questions | content_id, type, prompt, options_text, correct_answer, explanation, points, position. | Solución reservada para corrección y gestión. |
| exam_attempts | student_id, content_id, attempt_number, percent, stars, passed, created_at. | Numeración única por estudiante y contenido. |
| exam_answers | attempt_id, question_id, answer_text, correct, earned_points. | Una respuesta registrada por pregunta e intento. |
| discussions | content_id, author_id, parent_id, body, deleted, created_at. | Respuesta al mismo contenido; retiro lógico. |
| reactions | post_id, actor_id, type. | Una reacción actual por usuario y aporte. |
| ratings | post_id, actor_id, score. | Una valoración actual; rango de 1 a 5. |
| course_reviews | course_id, author_id, body, score, created_at. | Una reseña por autor y curso. |
| certificates | student_id, course_id, code, student_name, course_title, issued_at. | Código único de siete dígitos; emisión única. |
| audit_entries | actor_id, action, details, created_at. | Operación vinculada con su actor. |
| user_sessions | account_id, role_at_login, logged_in_at, logged_out_at, expires_at. | No almacena el identificador secreto de sesión. |

#### 3.2.5.4 Script de la base de datos

La definición ejecutable se organiza en cinco migraciones Flyway: V1 crea el esquema; V2 añade restricciones; V3 registra sesiones; V4 incorpora vigencia al pago; y V5 establece activación y rangos de evaluación. El historial permite reconocer qué cambios se aplicaron a cada base.

La matrícula se define en el script mediante identificadores, fechas y una restricción única:

```sql
create table enrollments (
 id bigint generated by default as identity primary key,
 version bigint not null default 0,
 student_id bigint not null references accounts(id),
 course_id bigint not null references courses(id),
 starts_at timestamp(6) not null,
 expires_at timestamp(6) not null,
 status varchar(30) not null,
 unique(student_id,course_id)
);
```

Las restricciones complementarias controlan estados y coherencia de fechas. Los datos de demostración se crean únicamente sobre una base vacía cuando esa configuración está habilitada.

### 3.2.6 ORGANIZACIÓN DE PAQUETES Y LIBRERÍAS

Los componentes se agrupan por responsabilidad. Las dependencias se utilizan para funciones identificables dentro de la aplicación.

**Tabla 11. Componentes y dependencias**

| Componente | Responsabilidad o tecnología |
|---|---|
| Frontend core | Modelos TypeScript, HttpClient, sesión, interceptor CSRF y guards. |
| Frontend shared | Modales reactivos, campos, estados y componentes reutilizables. |
| Frontend pages y rutas | Catálogo, acceso, paneles, gestión, aula y carga diferida. |
| API y configuración | Controladores REST, DTO, errores y Spring Security. |
| Servicios | Reglas, transacciones, archivos, evaluación y certificados. |
| Dominio y repositorios | Entidades, Spring Data JPA, Hibernate y consultas JPQL. |
| Esquema y base | Flyway y H2. |
| Presentación | Angular 22 y Tailwind CSS compilado localmente. |
| Backend y construcción | Java 21, Spring Boot 3.5.16 y Maven. |
| Documentos | PDFBox para PDF y ZXing para QR. |
| Pruebas | JUnit, Vitest, jsdom y Playwright. |

### 3.2.7 DOCUMENTACIÓN DE MÓDULOS

Los módulos comparten identidad, permisos y persistencia. Cada uno conserva una responsabilidad funcional.

#### 3.2.7.1 Identidad, usuarios y perfiles

Gestiona registro, acceso, salida, estado y rol de las cuentas. El registro público crea estudiantes y no acepta elevación de privilegios. El perfil permite editar información básica y cambiar contraseña validando la actual. La administración mantiene usuarios y profesores.

#### 3.2.7.2 Cursos y asignación de profesores

Administra información, publicación y asignaciones. Un profesor consulta y gestiona sus cursos autorizados. Los cursos con antecedentes se conservan; la eliminación se limita a registros vacíos y sin relaciones protegidas.

#### 3.2.7.3 Módulos y contenidos académicos

Organiza el temario mediante posiciones. Admite texto, video, material, actividad, foro y evaluación. La vista previa habilitada corresponde a los contenidos permitidos; el temario público no revela recursos privados.

#### 3.2.7.4 Pagos administrativos, inscripciones y acceso

Relaciona selección, solicitud pendiente, validación del pago y matrícula. La edición conserva estudiante y curso y utiliza control de versión. La vigencia se comprueba al acceder; su vencimiento conserva los antecedentes.

#### 3.2.7.5 Seguimiento del progreso

Registra una finalización por estudiante y contenido. Calcula el porcentaje a partir de registros reales y determina el siguiente contenido sin completar. Este indicador representa recorrido y permanece separado de notas y aprobación.

#### 3.2.7.6 Actividades y calificaciones

Recibe archivos privados y comentarios, conserva intentos y permite revisión docente. La nota admite valores de cero a veinte y retroalimentación. La descarga requiere titularidad o permiso de gestión.

#### 3.2.7.7 Evaluaciones formativas

Gestiona opción múltiple, verdadero/falso, relaciones, completar y selección de términos. Corrige según puntajes, conserva intentos, porcentaje, aprobación y estrellas. La configuración controla activación y límites. Los cuestionarios con historial protegen preguntas y umbral.

#### 3.2.7.8 Participación académica

Permite aportes, respuestas, edición propia, retiro lógico, reacciones y valoraciones. Controla el contenido al que pertenece cada respuesta y evita autovaloraciones. Incluye las funciones básicas disponibles de reseñas del curso.

#### 3.2.7.9 Certificación

Comprueba condiciones de culminación, registra una emisión única y genera PDF con código y QR. Conserva el nombre y título utilizados al emitir. La consulta y descarga respetan los controles establecidos y la identificación de demostración.

## 3.3 ARQUITECTURA DEL SISTEMA

La aplicación integra una interfaz Angular y un backend monolítico por capas. El mismo servidor entrega los recursos compilados y atiende la API, simplificando la ejecución local y la gestión de la sesión.

### 3.3.1 ARQUITECTURA POR CAPAS Y PATRONES DE DISEÑO

La presentación reúne componentes y formularios. Los controladores Spring MVC adaptan las solicitudes REST; los servicios coordinan reglas; los repositorios encapsulan el acceso a datos; y las entidades representan el dominio.

El patrón Repository cumple la función de acceso a datos, sin duplicarlo mediante un DAO artificial. Los DTO delimitan entradas y salidas. AcademicService actúa como punto de coordinación de casos de uso; su amplitud no se presenta como una separación completa por dominio.

La inyección de dependencias reduce la construcción manual de colaboradores. La separación de controladores, servicios y repositorios favorece responsabilidades claras, aunque no constituye una demostración de cumplimiento absoluto de todos los principios SOLID.

### 3.3.2 DIAGRAMA DE ARQUITECTURA DEL SISTEMA

Angular envía solicitudes HTTP al servidor. Spring Security valida la sesión y los controles de solicitud; los controladores invocan servicios y repositorios; Hibernate accede a H2. El almacenamiento privado y la generación de documentos se ejecutan desde servicios autorizados.

[[imagen 4: arquitectura con Angular, HttpClient, sesión y CSRF, controladores REST, servicios, JPA/Hibernate, H2, archivos privados y certificados PDF]]

El frontend compilado se incluye en un WAR ejecutable con Tomcat. Node.js y npm intervienen en la construcción de Angular; la ejecución del WAR utiliza Java. Durante el desarrollo, un proxy permite comunicar la interfaz con el backend.

### 3.3.3 INTERFACES Y CONTRATOS DE SERVICIOS

La API utiliza JSON para datos y multipart para archivos. Las respuestas limitan campos y omiten contraseñas, claves internas y soluciones no autorizadas.

**Tabla 12. Contratos principales de la API**

| Método y ruta | Entrada o finalidad | Resultado |
|---|---|---|
| GET /api/auth/csrf y /api/auth/session | Contexto de solicitud e identidad. | Token CSRF y datos de sesión seguros. |
| POST /api/auth/login | email, password y CSRF. | Sesión autenticada. |
| POST /api/auth/logout | Sesión y CSRF. | Invalidación y respuesta 204. |
| GET /api/cursos | Filtros de búsqueda. | Catálogo autorizado. |
| POST/PUT /api/admin/cursos | Datos del curso. | Creación 201 o actualización 200. |
| POST/PUT /api/admin/pagos | Estudiante, curso, importe, estado, vigencia y versión al editar. | Pago y matrícula consistentes. |
| GET /api/aula/cursos/{id} | Identificador y sesión. | Curso, temario y progreso autorizado. |
| POST /api/contenidos/{id}/completar | Identificador del contenido. | Finalización idempotente. |
| POST /api/contenidos/{id}/entregar | Archivo y comentario multipart. | Entrega registrada. |
| PUT /api/gestion/entregas/{id}/calificar | grade y feedback. | Calificación autorizada. |
| POST /api/contenidos/{id}/evaluar | Respuestas agrupadas por pregunta. | Intento y resultado persistidos. |
| GET /api/evaluaciones/intentos/{id} | Identificador del intento. | Resultado propio o de gestión. |
| GET /api/gestion/cursos/{id}/reporte.csv | Curso y permiso de gestión. | Archivo CSV. |
| POST /api/certificados/emitir/{id} | Curso y sesión de estudiante. | Certificado existente o nueva emisión única. |

Los errores presentan status, message y errors. Se distinguen 400 por datos inválidos, 401 por autenticación, 403 por permisos o CSRF, 404 por recurso inexistente, 409 por conflicto, 413 por tamaño y 422 por reglas no satisfechas. Angular muestra el mensaje y conserva los campos corregibles.

## 3.4 SEGURIDAD DEL SISTEMA

La seguridad combina identidad, permisos y validación de recursos. Ocultar un botón no sustituye la comprobación del servidor.

### 3.4.1 AUTENTICACIÓN

Spring Security valida credenciales protegidas con BCrypt. El acceso cambia el identificador de sesión y conserva el contexto de seguridad en el servidor. La cookie utiliza HttpOnly y SameSite; las credenciales no se guardan en localStorage.

Angular consulta la identidad y obtiene un token CSRF. Su interceptor añade el encabezado a las escrituras de la API. Después de iniciar o cerrar sesión recupera el contexto y token correspondientes. Las cuentas desactivadas o con permisos modificados deben autenticarse nuevamente.

### 3.4.2 ROLES Y PERMISOS

Los guards orientan la navegación. Spring Security y los servicios verifican además asignación, titularidad y vigencia.

**Tabla 13. Permisos por perfil**

| Operación | Visitante | Estudiante | Profesor | Administrador |
|---|---|---|---|---|
| Catálogo publicado | Sí | Sí | Sí | Sí |
| Aprendizaje y entregas propias | Vista previa permitida | Con matrícula vigente | Consulta autorizada | Consulta autorizada |
| Gestión de contenidos y calificaciones | No | No | Cursos asignados | Sí |
| Usuarios, pagos y matrículas | No | Registros propios de consulta | No | Sí |
| Certificados | No | Emisión propia según condiciones | Sin emisión de estudiante | Consulta administrativa |
| Auditoría, sesiones y vista de perfiles | No | No | No | Sí |

La vista administrativa de perfiles es de solo lectura y mantiene la identidad del administrador. Una solicitud directa a la API aplica los mismos permisos aunque no se origine desde un menú.

### 3.4.3 VALIDACIÓN Y PROTECCIÓN DE LA INFORMACIÓN

Jakarta Validation y los servicios comprueban campos, rangos y relaciones. Las claves únicas y transacciones complementan la validación. Los errores no devuelven trazas internas.

Los archivos admiten formatos restringidos de hasta 10 MB, con comprobación básica de extensión y firma para los formatos aplicables. Se almacenan con nombres generados y se descargan mediante controladores. Esta protección no equivale a un análisis antivirus.

La interfaz trata los aportes como texto y la política de contenido limita recursos ejecutables. Los CSV neutralizan celdas que podrían interpretarse como fórmulas. Las lecturas públicas no incluyen cuerpos privados ni respuestas correctas; estas se entregan únicamente en gestión o resultados autorizados.

## 3.5 PRUEBAS Y VALIDACIÓN

La validación combina componentes aislados, servicios con persistencia y recorridos reales. Las bases de prueba y sus archivos se separan de los datos habituales.

### 3.5.1 DESARROLLO GUIADO POR PRUEBAS — TDD

Se aplicó un ciclo inicial sobre ExamGrader: se definieron seis casos, se verificó el fallo por ausencia de implementación y se desarrolló la corrección hasta obtener resultados satisfactorios. En la API se comprobó un conjunto inicial de contratos que detectó funciones pendientes antes de completarlas.

Las demás verificaciones incluyen regresión y pruebas añadidas durante el desarrollo. La existencia de una suite satisfactoria no acredita que cada línea se haya construido mediante un ciclo TDD independiente.

### 3.5.2 PRUEBAS UNITARIAS

Las pruebas de corrección verifican opciones, normalización, relaciones, espacios, respuestas faltantes y estrellas. Las de almacenamiento comprueban formatos, nombres y rutas; las de certificados verifican PDF, acentos y lectura del QR.

En Angular se prueban peticiones, filtros, CSRF, acceso, guards y errores. El modal verifica validaciones, conservación de datos, identidades deshabilitadas y reapertura de formularios.

### 3.5.3 PRUEBAS DE INTEGRACIÓN

Las pruebas Java relacionan servicios, repositorios, transacciones y seguridad. Se comprueban pagos, acceso, intentos, permisos, historial y certificados. Un escenario concurrente ejecuta seis solicitudes con un límite de tres intentos y verifica que no existan números duplicados.

Las pruebas HTTP utilizan Tomcat real, cookies, CSRF, multipart y descargas. La suite Java también conserva comprobaciones de renderizado de vistas del servidor; esas pruebas complementan las verificaciones REST y no se contabilizan como recorridos Angular. Se mantuvo una sesión válida durante doce escrituras HTTP consecutivas.

### 3.5.4 PRUEBAS FUNCIONALES Y RESULTADOS

Los resultados corresponden a la ejecución comprobada, sin convertir el número de casos en porcentaje de cobertura.

**Tabla 14. Resultados de pruebas**

| Grupo | Cantidad | Resultado |
|---|---:|---|
| AcademicIntegrationTest | 31 | Satisfactorio. |
| PaymentAcademicRulesTest | 12 | Satisfactorio. |
| ExamGraderTest | 6 | Satisfactorio. |
| StorageServiceTest | 5 | Satisfactorio. |
| CertificatePdfServiceTest | 2 | Satisfactorio. |
| ConcurrentAttemptsTest | 1 | Satisfactorio. |
| MvcHttpSmokeTest | 15 | Satisfactorio. |
| ApiIntegrationTest | 13 | Satisfactorio. |
| **Total Java** | **85** | **Sin fallos, errores u omisiones.** |
| **Angular** | **15** | **Todas satisfactorias.** |
| **Recorridos Chromium** | **5** | **Todos satisfactorios.** |

Los recorridos comprueban: edición persistente y eliminación segura; catálogo con vacío, fallo y recuperación; flujo completo de administración y aprendizaje; rutas, perfil y registro; y carrito con solicitud pendiente.

El flujo completo crea curso, módulo, seis tipos de contenido y cinco tipos de pregunta; registra y edita un pago; habilita matrícula; procesa material, entrega y foro; obtiene 100 % en la evaluación de prueba; registra una nota docente de 18.5; exporta CSV; y emite y valida un PDF. Otro estudiante recibe 403 al intentar descargar evidencias ajenas.

[[imagen 5: resultados verificables de 85 pruebas Java, 15 pruebas Angular y cinco recorridos Chromium satisfactorios]]

También se recargaron once pantallas de los tres perfiles sin errores JavaScript. Las muestras móviles comprobaron menú y cuenta inferior. No se derivan de estos resultados garantías de producción, satisfacción institucional o compatibilidad universal.

## 3.6 GESTIÓN DE VERSIONAMIENTO CON GIT Y GITHUB

Git conserva estados del código y permite identificar cambios mediante commits (Git, s. f.). [Control de versiones](https://git-scm.com/book/en/v2/Getting-Started-About-Version-Control). El remoto origin corresponde al [repositorio del proyecto](https://github.com/wsucari/utp_desarrollo_web_integrado).

### 3.6.1 RAMIFICACIÓN — BRANCHING

La rama de trabajo es main y existe avance_2 como resguardo del código. La implementación Angular y REST está incorporada en main y publicada en origin/main. El respaldo avance_2 conserva el commit 2836441efec05969f5710d1d88ba4a580084df61 y no recibe las modificaciones de la integración.

### 3.6.2 ESTRATEGIA DE FUSIÓN — MERGE

La implementación se registró directamente en main. No se realizaron fusiones entre ramas ni se presenta una resolución de conflictos no ejecutada. La integración funcional demostrada corresponde a los componentes de la aplicación y sus contratos HTTP.

### 3.6.3 COMMITS

La integración Angular y REST está registrada en el commit 6208a8b046f51ef59018251779a47110a53f924f, publicado en origin/main. Este registro incluye frontend, API, configuración, pruebas y documentación técnica. La documentación formal y las figuras se incorporan al mismo repositorio. La base local, los archivos privados y las dependencias instaladas quedan excluidos del control de versiones.

### 3.6.4 INCIDENCIAS — ISSUES

Los hallazgos técnicos y las limitaciones se conservan en documentación local. No se aporta un historial verificable de incidencias creadas, asignadas y cerradas en GitHub, por lo que no se atribuye a esa plataforma un seguimiento no demostrado.

# CONCLUSIONES

1. El análisis permitió relacionar actores, procesos y requisitos con una estructura de cursos, acceso y evidencias. La trazabilidad facilita identificar qué operación atiende cada necesidad.
2. La arquitectura integra Angular y servicios REST con persistencia relacional. Los formularios, estados y errores se comunican con operaciones reales del servidor, manteniendo sus reglas fuera de la interfaz.
3. La separación entre pago y matrícula permite controlar vigencia y conservar antecedentes. Las transacciones y el control de versión protegen la consistencia de las modificaciones.
4. El sistema distingue recorrido y desempeño: progreso, entregas, calificaciones e intentos conservan finalidades e historial propios. La certificación aplica condiciones explícitas y evita emisiones duplicadas.
5. La autorización combina rol, asignación docente, titularidad y acceso vigente. Las pruebas comprobaron restricciones sobre datos y descargas, además de operaciones satisfactorias.
6. Las 85 pruebas Java, 15 Angular y cinco recorridos de navegador respaldan los escenarios evaluados. El resultado es una aplicación local verificable, con límites documentados y sin afirmar impacto institucional o preparación productiva.

# RECOMENDACIONES

Se recomienda completar la ampliación de perfiles y reseñas y revisar los detalles de imágenes, filtros, paginación y sesiones mediante criterios de aceptación específicos, manteniendo los permisos y la conservación de información.

Se recomienda continuar registrando los cambios reales en Git, asociando cada estado del código con sus pruebas y documentación. Los mensajes y fechas deben representar operaciones efectivamente realizadas y el respaldo de código debe conservarse sin incorporar modificaciones posteriores.

Antes de un despliegue público, se requiere evaluar HTTPS, cookies seguras, secretos, respaldo conjunto de base y archivos, recuperación y protección adicional de cargas. La portabilidad a otro motor debe comprobarse con su controlador, scripts y pruebas.

Finalmente, una evaluación con usuarios representativos permitiría conocer dificultades de uso. Las mediciones de satisfacción, accesibilidad y rendimiento deben realizarse por separado y no inferirse del número de pruebas aprobadas.

# BIBLIOGRAFÍA

Angular. (s. f.-a). *[Control route access with guards](https://angular.dev/guide/routing/route-guards)*.

Angular. (s. f.-b). *[HTTP Client](https://angular.dev/guide/http)*.

Angular. (s. f.-c). *[Reactive forms](https://angular.dev/guide/forms/reactive-forms)*.

Fowler, M. (2023, 11 de diciembre). *[Test driven development](https://martinfowler.com/bliki/TestDrivenDevelopment.html)*.

Git. (s. f.). *[About version control](https://git-scm.com/book/en/v2/Getting-Started-About-Version-Control)*.

H2 Database Engine. (s. f.). *[Features](https://www.h2database.com/html/features.html)*.

Hibernate. (s. f.). *[Hibernate ORM user guide](https://docs.hibernate.org/orm/6.6/userguide/html_single/)*.

Inudi Perú. (s. f.). *[¿Quiénes somos?](https://inudi.edu.pe/nosotros/)*.

Spring. (s. f.-a). *[Cross Site Request Forgery (CSRF)](https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html)*.

Spring. (s. f.-b). *[@ResponseBody](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html)*.

Spring. (s. f.-c). *[Structuring your code](https://docs.spring.io/spring-boot/3.5/reference/using/structuring-your-code.html)*.

Spring. (s. f.-d). *[Transactionality](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html)*.

# ANEXOS

**Anexo A. Ejecución local**

La construcción requiere JDK 21, Maven y una versión de Node.js compatible con Angular 22. Desde la raíz del código se preparan dependencias, compila la interfaz y empaqueta la aplicación:

```powershell
npm ci
npm --prefix frontend ci
npm run build:ui
mvn -B package
java -jar target/inudi-academico.war
```

La aplicación se consulta en http://127.0.0.1:8080. El WAR incluye el frontend compilado y el servidor; su ejecución no requiere Node.js. Las cuentas y cursos de demostración se crean únicamente en una base vacía con esa opción habilitada.

**Anexo B. Conservación de datos**

La información habitual utiliza H2 persistente y almacenamiento privado. El respaldo debe realizarse con el servidor detenido e incluir conjuntamente base y archivos. Copiar solamente el código no conserva matrículas, evidencias o documentos.

**Anexo C. Reproducción de pruebas**

Las suites se ejecutan con los siguientes comandos, después de construir el WAR y preparar Chromium para Playwright:

```powershell
mvn -B test
npm run test:ui
npm --prefix frontend run install:browser
npm run test:e2e
```

Las pruebas de navegador levantan un servidor aislado en el puerto 8093 con H2 en memoria y lo detienen al finalizar. Los reportes detallan casos, estados y resultados. Los cinco recorridos no representan un porcentaje de cobertura de todo el sistema.

**Anexo D. Evidencias y evaluación de uso**

Las cinco figuras corresponden a casos de uso, interfaz, datos, arquitectura y resultados. Los diagramas representan el diseño; las capturas y reportes respaldan comportamientos ejecutados. El instrumento de encuesta siguiente forma parte de la evaluación propuesta y no se presenta como una encuesta aplicada.

# ENCUESTA

**Instrumento de valoración de la experiencia de uso**

El instrumento recoge percepciones sobre tareas correspondientes al perfil del participante. Su finalidad es identificar dificultades de navegación, comprensión y retroalimentación. La participación no requiere entregar contraseñas, documentos de identidad ni datos económicos.

La escala es: 1, totalmente en desacuerdo; 2, en desacuerdo; 3, ni de acuerdo ni en desacuerdo; 4, de acuerdo; y 5, totalmente de acuerdo. Se admite «no aplica» cuando la función no corresponde al perfil.

**Tabla 15. Instrumento de valoración de uso**

| N.° | Afirmación |
|---|---|
| 1 | Identifico las opciones que corresponden a mi perfil. |
| 2 | Reconozco en qué parte del sistema me encuentro. |
| 3 | Los nombres de las opciones describen sus acciones. |
| 4 | Las etiquetas y campos de los formularios son comprensibles. |
| 5 | Los mensajes de error me ayudan a corregir los datos. |
| 6 | Los modales permiten trabajar sin perder el contexto. |
| 7 | Comprendo el estado de mi matrícula y su vigencia. |
| 8 | Distingo progreso, notas de actividades y resultados de evaluaciones. |
| 9 | Los controles son utilizables desde mi pantalla. |
| 10 | Los mensajes de confirmación permiten reconocer que la operación terminó. |

Las preguntas abiertas son: ¿qué tarea resultó más difícil?, ¿qué información no fue clara? y ¿qué mejora facilitaría el uso del sistema?

No se incluyen resultados estadísticos porque no se documentó la aplicación de este instrumento. Las conclusiones técnicas del proyecto proceden de las pruebas y recorridos descritos.
