package pe.edu.inudi.web;

import static org.assertj.core.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pe.edu.inudi.domain.*;
import pe.edu.inudi.repository.*;

/**
 * Prueba HTTP real sobre Tomcat: compila y renderiza las JSP, a diferencia de MockMvc, que valida
 * los controladores sin ejecutar el motor JSP.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.datasource.url=jdbc:h2:mem:http-tests;DB_CLOSE_DELAY=-1",
      "app.demo.enabled=true",
      "app.storage=target/http-test-uploads"
    })
class MvcHttpSmokeTest {
  @LocalServerPort int port;
  @Autowired CourseRepository courses;
  @Autowired ContentRepository contents;
  @Autowired LearningModuleRepository modules;
  @Autowired QuestionRepository questions;
  @Autowired SubmissionRepository submissions;
  @Autowired AccountRepository accounts;
  @Autowired CertificateRepository certificates;

  String url(String path) {
    return "http://127.0.0.1:" + port + path;
  }

  HttpClient client() {
    return HttpClient.newBuilder()
        .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
  }

  HttpResponse<String> get(HttpClient client, String path) throws Exception {
    return client.send(
        HttpRequest.newBuilder(URI.create(url(path))).GET().build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  String token(String html) {
    var matcher = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(html);
    assertThat(matcher.find()).as("Token CSRF presente").isTrue();
    return matcher.group(1);
  }

  HttpClient login(String email) throws Exception {
    HttpClient client = client();
    String csrf = token(get(client, "/login").body());
    var result =
        client.send(
            HttpRequest.newBuilder(URI.create(url("/login")))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        "username="
                            + URLEncoder.encode(email, StandardCharsets.UTF_8)
                            + "&password=Inudi2026%21Demo&_csrf="
                            + URLEncoder.encode(csrf, StandardCharsets.UTF_8)))
                .build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(result.statusCode()).isEqualTo(303);
    String destination = email.startsWith("admin") ? "/dashboard" : email.startsWith("docente")
        ? "/profesor/dashboard" : "/users/panel/inicio";
    assertThat(result.headers().firstValue("Location").orElse("")).endsWith(destination);
    return client;
  }

  void pages(HttpClient client, List<String> paths) throws Exception {
    for (String path : paths) {
      var result = get(client, path);
      assertThat(result.statusCode())
          .as(
              "Página JSP "
                  + path
                  + " / "
                  + result.body().substring(0, Math.min(100, result.body().length())))
          .isEqualTo(200);
      assertThat(result.body())
          .doesNotContain("Ã³", "Ã©", "Â·", "Whitelabel Error Page", "JasperException");
      assertThat(result.body())
          .doesNotContain("sidebar-note", "NUESTRO PROPÓSITO", "Conocimiento que transforma.", "/verificar");
    }
  }

  HttpResponse<String> post(HttpClient client, String path, String fields, String formPage)
      throws Exception {
    String csrf = token(get(client, formPage).body());
    return client.send(
        HttpRequest.newBuilder(URI.create(url(path)))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    fields
                        + (fields.isEmpty() ? "" : "&")
                        + "_csrf="
                        + URLEncoder.encode(csrf, StandardCharsets.UTF_8)))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  String encode(String text) {
    return URLEncoder.encode(text, StandardCharsets.UTF_8);
  }

  @Test
  void publicJspRenderWithSpanishEncoding() throws Exception {
    var client = client();
    Long cid = courses.findAll().getFirst().getId();
    Long preview =
        contents.inCourse(cid).stream()
            .filter(Content::isPreview)
            .findFirst()
            .orElseThrow()
            .getId();
    pages(
        client,
        List.of(
            "/login",
            "/registro",
            "/",
            "/catalogo/" + cid,
            "/vista-previa/" + preview));
    assertThat(get(client, "/").statusCode()).isEqualTo(200);
    assertThat(get(client, "/").body()).contains("¿Qué deseas lograr hoy?");
    assertThat(get(client, "/cert").statusCode()).isEqualTo(302);
  }

  @Test
  void loginUsesCompiledTailwindAndReferenceCardStructure() throws Exception {
    var client = client();
    var page = get(client, "/login");
    assertThat(page.statusCode()).isEqualTo(200);
    assertThat(page.body())
        .contains("/assets/tailwind.css", "tw-max-w-md", "Plataforma Inudi",
            "Iniciar Sesión", "data-password-toggle", "name=\"_csrf\"")
        .doesNotContain("auth-story", "Validar certificado", "Tu formación no termina");
    var css = get(client, "/assets/tailwind.css");
    assertThat(css.statusCode()).isEqualTo(200);
    assertThat(css.body()).contains(".tw-max-w-md", ".tw-bg-gray-200", "--tw-ring-color");
  }

  @Test
  void studentPagesActuallyRender() throws Exception {
    var client = login("estudiante@inudi.local");
    Long cid = courses.findAll().getFirst().getId();
    var paths =
        new ArrayList<>(
            List.of(
                "/panel",
                "/perfil",
                "/mis-cursos",
                "/carrito",
                "/mis-pagos",
                "/certificados",
                "/aula/cursos/" + cid));
    for (Content c : contents.inCourse(cid)) paths.add("/contenidos/" + c.getId());
    pages(client, paths);
  }

  @Test
  void adminFormsAndManagementJspActuallyRender() throws Exception {
    var client = login("admin@inudi.local");
    Long cid = courses.findAll().getFirst().getId();
    Long mid = modules.findByCourseIdOrderByPositionAscIdAsc(cid).getFirst().getId();
    Long lesson = contents.inCourse(cid).getFirst().getId();
    Long exam =
        contents.inCourse(cid).stream()
            .filter(c -> c.getType() == ContentType.EVALUACION)
            .findFirst()
            .orElseThrow()
            .getId();
    pages(
        client,
        List.of(
            "/panel",
            "/admin/usuarios",
            "/admin/usuarios/nuevo",
            "/admin/usuarios/1/editar",
            "/admin/cursos/nuevo",
            "/admin/cursos/" + cid + "/editar",
            "/admin/pagos",
            "/admin/matriculas",
            "/admin/certificados",
            "/admin/auditoria",
            "/gestion/cursos",
            "/gestion/cursos/" + cid,
            "/gestion/cursos/" + cid + "/seguimiento",
            "/gestion/modulos/" + mid + "/contenidos/nuevo",
            "/gestion/contenidos/" + lesson + "/editar",
            "/gestion/contenidos/" + exam + "/preguntas"));
  }

  @Test
  void teacherPagesAndCsvExportActuallyWork() throws Exception {
    var client = login("docente@inudi.local");
    Long cid = courses.findAll().getFirst().getId();
    pages(
        client,
        List.of(
            "/panel",
            "/gestion/cursos",
            "/gestion/cursos/" + cid,
            "/gestion/cursos/" + cid + "/seguimiento"));
    var csv = get(client, "/gestion/cursos/" + cid + "/reporte.csv");
    assertThat(csv.statusCode()).isEqualTo(200);
    assertThat(csv.body())
        .contains("Estudiante;Correo;Curso;Estado;Progreso;Vencimiento", "Alex Rivera");
  }

  @Test
  void actualMultipartSubmissionAcceptsCsrfInsideForm() throws Exception {
    var client = login("estudiante@inudi.local");
    Long cid = courses.findAll().getFirst().getId();
    Long activity =
        contents.inCourse(cid).stream()
            .filter(c -> c.getType() == ContentType.ACTIVIDAD)
            .findFirst()
            .orElseThrow()
            .getId();
    String csrf = token(get(client, "/contenidos/" + activity).body()),
        boundary = "inudi-test-boundary";
    String body =
        "--"
            + boundary
            + "\r\nContent-Disposition: form-data; name=\"_csrf\"\r\n\r\n"
            + csrf
            + "\r\n--"
            + boundary
            + "\r\nContent-Disposition: form-data; name=\"comment\"\r\n\r\nActividad de prueba HTTP\r\n--"
            + boundary
            + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"propuesta.txt\"\r\nContent-Type: text/plain\r\n\r\nObjetivo: organizar una propuesta.\r\n--"
            + boundary
            + "--\r\n";
    var result =
        client.send(
            HttpRequest.newBuilder(URI.create(url("/contenidos/" + activity + "/entregar")))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(result.statusCode()).as(result.body()).isEqualTo(302);
    assertThat(get(client, "/contenidos/" + activity).body())
        .contains("propuesta.txt", "Por calificar");
  }

  @Test
  void completeLearningJourneyProducesDownloadableValidatedCertificate() throws Exception {
    var client = login("estudiante@inudi.local");
    Long cid =
        courses.findAll().stream()
            .filter(c -> c.getCategory().equals("Producción científica"))
            .findFirst()
            .orElseThrow()
            .getId();
    var lessons = contents.inCourse(cid);
    var activity =
        lessons.stream()
            .filter(c -> c.getType() == ContentType.ACTIVIDAD)
            .findFirst()
            .orElseThrow();
    var forum =
        lessons.stream().filter(c -> c.getType() == ContentType.FORO).findFirst().orElseThrow();
    var exam =
        lessons.stream()
            .filter(c -> c.getType() == ContentType.EVALUACION)
            .findFirst()
            .orElseThrow();
    String page = "/contenidos/" + activity.getId(),
        csrf = token(get(client, page).body()),
        boundary = "journey-boundary";
    String multipart =
        "--"
            + boundary
            + "\r\nContent-Disposition: form-data; name=\"_csrf\"\r\n\r\n"
            + csrf
            + "\r\n--"
            + boundary
            + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"propuesta-final.txt\"\r\nContent-Type: text/plain\r\n\r\nObjetivos y estructura de la investigación.\r\n--"
            + boundary
            + "--\r\n";
    var uploaded =
        client.send(
            HttpRequest.newBuilder(URI.create(url(page + "/entregar")))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(multipart))
                .build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(uploaded.statusCode()).isEqualTo(302);
    var submission = submissions.findByContentIdOrderByCreatedAtDesc(activity.getId()).getFirst();
    var teacher = login("docente@inudi.local");
    assertThat(
            post(
                    teacher,
                    "/gestion/entregas/" + submission.getId() + "/calificar",
                    "grade=18.5&feedback=" + encode("Propuesta clara y coherente"),
                    "/gestion/cursos/" + cid + "/seguimiento")
                .statusCode())
        .isEqualTo(302);
    assertThat(get(client, page).body()).contains("18.50", "Propuesta clara y coherente");
    String forumPage = "/contenidos/" + forum.getId();
    String hostile = "<script>alert('no ejecutar')</script>";
    assertThat(
            post(client, forumPage + "/publicaciones", "body=" + encode(hostile), forumPage)
                .statusCode())
        .isEqualTo(302);
    assertThat(get(client, forumPage).body()).contains("&lt;script&gt;").doesNotContain(hostile);
    StringBuilder fields = new StringBuilder();
    for (var q : questions.findByContentIdOrderByPositionAscIdAsc(exam.getId()))
      for (String key : q.getCorrectAnswer().split("\\|")) {
        if (!fields.isEmpty()) fields.append('&');
        fields.append("q_").append(q.getId()).append('=').append(encode(key));
      }
    var evaluated =
        post(
            client,
            "/contenidos/" + exam.getId() + "/evaluar",
            fields.toString(),
            "/contenidos/" + exam.getId());
    assertThat(evaluated.statusCode()).isEqualTo(302);
    String result = evaluated.headers().firstValue("Location").orElseThrow();
    assertThat(get(client, URI.create(result).getPath()).body()).contains("100.00", "APROBADO");
    for (var c : lessons)
      assertThat(
              post(
                      client,
                      "/contenidos/" + c.getId() + "/completar",
                      "",
                      "/contenidos/" + c.getId())
                  .statusCode())
          .as(c.getTitle())
          .isEqualTo(302);
    assertThat(get(client, "/aula/cursos/" + cid).body()).contains("100%");
    assertThat(post(client, "/certificados/emitir/" + cid, "", "/certificados").statusCode())
        .isEqualTo(302);
    var student = accounts.findByEmailIgnoreCase("estudiante@inudi.local").orElseThrow();
    var certificate = certificates.findByStudentIdAndCourseId(student.getId(), cid).orElseThrow();
    var download =
        client.send(
            HttpRequest.newBuilder(URI.create(url("/certificados/" + certificate.getId() + "/pdf")))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofByteArray());
    assertThat(download.statusCode()).isEqualTo(200);
    try (var pdf = org.apache.pdfbox.Loader.loadPDF(download.body())) {
      assertThat(new org.apache.pdfbox.text.PDFTextStripper().getText(pdf))
          .contains(certificate.getCode(), "DEMOSTRACIÓN LOCAL", "Alex Rivera");
    }
    assertThat(get(client, "/cert/" + certificate.getCode()).body())
        .contains("Certificado válido", "Alex Rivera");
    assertThat(
            get(login("camila@inudi.local"), "/certificados/" + certificate.getId() + "/pdf")
                .statusCode())
        .isEqualTo(403);
  }

  @Test
  void originalMenusUseInstitutionalLogoAndDoNotInventSidebarItems() throws Exception {
    var student = login("estudiante@inudi.local");
    String studentPage = get(student, "/users/panel/inicio").body();
    assertThat(studentPage).contains("/images/logos/inudi_white.webp", "/images/logos/inudiblack.png",
        "Cursos Inscritos", "Lista de deseos", "Mis comentarios", "Carritos", "Ajustes", "Salir")
        .doesNotContain("Gestión de cursos", "Seguimiento académico", "NUESTRO PROPÓSITO",
            "Conocimiento que transforma", "APRENDER. INVESTIGAR. TRANSFORMAR.");
    pages(student, List.of("/users/panel/perfil", "/users/panel/inscritos", "/users/panel/comentarios", "/users/carrito"));
    var teacher = login("docente@inudi.local");
    String teacherPage = get(teacher, "/profesor/dashboard").body();
    assertThat(teacherPage).contains("Menú principal", "Mis cursos", "Mi perfil", "Cursos asignados",
        "Gestionar contenido", "/images/logos/inudi_white.webp")
        .doesNotContain("Seguimiento académico", "Gestión de cursos", "Enseñar también es inspirar");
    pages(teacher, List.of("/profesor/cursos", "/profesor/perfil"));
    assertThat(get(student, "/profesor/dashboard").statusCode()).isEqualTo(403);
    assertThat(get(teacher, "/users/panel/inicio").statusCode()).isEqualTo(403);
    var admin = login("admin@inudi.local");
    String adminPage = get(admin, "/dashboard").body();
    assertThat(adminPage).contains("Panel de Administración", "Detalle de Cursos Registrados",
        "Menú de administración", "Cursos", "Usuarios", "Pagos", "Reseñas",
        "Profesores", "Ver como", "Sesiones", "Menú lateral").doesNotContain("welcome-banner");
    pages(admin, List.of("/admin/cursos", "/admin/user", "/admin/resenas", "/admin/profesores",
        "/admin/vista-previa", "/admin/sesiones"));
    assertThat(get(student, "/admin/resenas").statusCode()).isEqualTo(403);
    assertThat(get(client(), "/images/logos/inudiwhite.png").statusCode()).isEqualTo(200);
  }

  @Test
  void catalogReproducesSourceStructureAndSupportsOriginalFilters() throws Exception {
    var client = client();
    var page = get(client, "/");
    assertThat(page.body()).contains("InudiClass", "¿Qué deseas lograr hoy?", "Rutas académicas",
        "Investigación Científica", "Tesis y Grados", "Estadística y Datos", "IA para Investigación",
        "Docencia Universitaria", "Publicación Académica", "Más filtros", "Cursos destacados",
        "/images/logos/inudi_white.webp")
        .doesNotContain("Explora tu próximo paso", "Conocimiento y herramientas", "art-orbit");
    assertThat(get(client, "/?search=ZZZnoexiste").body()).contains("No encontramos cursos con esos criterios");
    assertThat(get(client, "/?acceso=inscritos").body()).contains("0 resultados disponibles.");
    assertThat(get(client, "/?search=%3Cscript%3E").body())
        .contains("&lt;script&gt;").doesNotContain("value=\"<script>\"");
    var student = login("estudiante@inudi.local");
    assertThat(get(student, "/?acceso=inscritos").body()).contains("Inscrito", "Continuar curso");
    assertThat(get(student, "/verificar").statusCode()).isEqualTo(404);
  }

  private String element(String html, String open, String close) {
    int start = html.indexOf(open);
    assertThat(start).as("Elemento " + open).isGreaterThanOrEqualTo(0);
    return html.substring(start, html.indexOf(close, start) + close.length());
  }

  @Test
  void headerIsSharedByPublicAndEveryRoleAndAdminItemsAreOnlyInSidebar() throws Exception {
    var guest = client();
    String publicPage = get(guest, "/").body();
    String publicHeader = element(publicPage, "<header", "</header>");
    assertThat(publicHeader).contains("id=\"site-header\"", "Inudi Perú", "Iniciar Sesión",
        "/images/logos/inudi_white.webp", "data-nav-toggle")
        .doesNotContain("<nav", "Nosotros", "Programas", "Eventos", "Editorial");
    for (String email : List.of("admin@inudi.local", "docente@inudi.local", "estudiante@inudi.local")) {
      var signedIn = login(email);
      String path = email.startsWith("admin") ? "/dashboard"
          : email.startsWith("docente") ? "/profesor/dashboard" : "/users/panel/inicio";
      for (String pagePath : List.of(path, "/")) {
        String html = get(signedIn, pagePath).body();
        String header = element(html, "<header", "</header>");
        assertThat(header).contains("id=\"site-header\"", "tw-bg-[#042354]", "Inudi Perú",
            "id=\"desktop-account-dropdown\"", "aria-controls=\"sidebar\"")
            .doesNotContain("<nav", "/admin/cursos", "/admin/user", "/admin/pagos", "/admin/resenas",
                "/profesor/cursos", "Cursos Online", "Revistas", "Libros");
        assertThat(html).contains("id=\"mobile-account-dropdown\"", "id=\"navigation-backdrop\"")
            .doesNotContain("id=\"account-dropdown\"", "id=\"campus-menu\"", "id=\"admin-menu\"",
                "id=\"institution-menu\"");
      }
    }
    var admin = login("admin@inudi.local");
    String html = get(admin, "/dashboard").body();
    String nav = element(html, "<nav aria-label=\"Menú lateral\"", "</nav>");
    var labels = new ArrayList<String>();
    var matcher = Pattern.compile("<span>([^<]+)</span></a>").matcher(nav);
    while (matcher.find()) labels.add(matcher.group(1));
    assertThat(labels).containsExactly("Inicio", "Cursos", "Usuarios", "Pagos", "Reseñas",
        "Profesores", "Ver como", "Sesiones");
  }

  @Test
  void legacyCatalogRoutesRedirectToRootPreservingFiltersAndDetailPaths() throws Exception {
    var client = client();
    for (String alias : List.of("/cursos", "/catalogo")) {
      var response = get(client, alias + "?ruta=tesis&search=investigaci%C3%B3n&nivel=Inicial");
      assertThat(response.statusCode()).isEqualTo(302);
      var location = URI.create(response.headers().firstValue("Location").orElseThrow());
      assertThat(location.getPath()).isEqualTo("/");
      assertThat(location.getRawQuery()).isEqualTo("ruta=tesis&search=investigaci%C3%B3n&nivel=Inicial");
      assertThat(get(client, URI.create(location.toString()).getRawPath() + "?" + location.getRawQuery()).statusCode())
          .isEqualTo(200);
    }
    String root = get(client, "/").body();
    assertThat(root).contains("action=\"/\"", "href=\"/?")
        .doesNotContain("action=\"/cursos\"", "href=\"/catalogo\"", "href=\"/cursos\"");
    var course = courses.findAll().getFirst();
    assertThat(get(client, "/cursos/" + course.getId()).statusCode()).isEqualTo(200);
  }

  @Test
  void mobileAccountIsAfterNavigationAndResponsiveRulesAreCompiled() throws Exception {
    var admin = login("admin@inudi.local");
    String html = get(admin, "/dashboard").body();
    String sidebar = element(html, "<aside", "</aside>");
    assertThat(sidebar.indexOf("id=\"mobile-account\"")).isGreaterThan(sidebar.indexOf("</nav>"));
    assertThat(sidebar).contains("sidebar-account", "account-dropup", "sidebar-link", "is-current")
        .doesNotContain("tw-text-green-600", "tw-border-green-500", "hover:tw-bg-gray-100' :");
    String css = get(client(), "/assets/tailwind.css").body();
    assertThat(css).contains(".sidebar-account", ".account-dropup", ".sidebar-link.is-current",
        "safe-area-inset-bottom", "border-radius:2px!important", "min-height:44px", "@container");
    String js = get(client(), "/assets/app.js").body();
    assertThat(js).contains("Tabla con desplazamiento horizontal", "closeAccountMenus", "Escape");
    pages(admin, List.of("/admin/cursos/nuevo", "/admin/usuarios/nuevo", "/admin/pagos"));
  }

  @Test
  void createEditActionsOpenModalAndPaymentsHaveEditableForm() throws Exception {
    var admin = login("admin@inudi.local");
    for (String path:List.of("/admin/cursos","/admin/user","/admin/pagos","/gestion/cursos/1","/gestion/contenidos/6/preguntas")) {
      String html=get(admin,path).body();
      assertThat(html).contains("id=\"editor-modal\"", "data-form-modal");
    }
    String payments=get(admin,"/admin/pagos").body();
    assertThat(payments).contains("Registrar pago","Editar","payments-table")
        .doesNotContain("<summary>", "Registrar como pendiente");
    pages(admin,List.of("/admin/pagos/nuevo"));
    String formPage="/admin/pagos/nuevo";
    assertThat(get(admin,formPage).body()).contains("value=\"" + java.time.LocalDate.now().plusYears(1) + "\"");
    var course=courses.findAll().stream().filter(c->c.getCategory().equals("Tecnología")).findFirst().orElseThrow();
    var student=accounts.findByEmailIgnoreCase("camila@inudi.local").orElseThrow();
    var saved=post(admin,formPage,"studentId="+student.getId()+"&courseId="+course.getId()
        +"&amount=90.00&reference=ModalHTTP&status=CONFIRMADO&expiration=2028-12-31",formPage);
    assertThat(saved.statusCode()).as(saved.body()).isEqualTo(302);
    assertThat(get(admin,"/admin/pagos").body()).contains("ModalHTTP","90.00");
    assertThat(get(login("estudiante@inudi.local"),"/admin/pagos/nuevo").statusCode()).isEqualTo(403);
  }

  @Test
  void previewDoesNotChangeIdentityAndSessionsRecordRealLoginLogout() throws Exception {
    var admin = login("admin@inudi.local");
    var studentAccount = accounts.findByEmailIgnoreCase("estudiante@inudi.local").orElseThrow();
    String preview = "/admin/vista-previa/estudiante/" + studentAccount.getId();
    assertThat(get(admin, preview).body()).contains("solo lectura", "Alex Rivera");
    assertThat(get(admin, "/dashboard").body()).contains("Panel de Administración");
    var student = login("estudiante@inudi.local");
    assertThat(get(student, preview).statusCode()).isEqualTo(403);
    assertThat(post(student, "/logout", "", "/users/panel/inicio").statusCode()).isEqualTo(303);
    assertThat(get(admin, "/admin/sesiones?sesion=sesion%20terminada&search=estudiante").body())
        .contains("estudiante@inudi.local", "Sesión terminada");
  }
}
