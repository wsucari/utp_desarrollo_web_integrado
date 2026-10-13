package pe.edu.inudi.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.inudi.domain.*;

@Service
@RequiredArgsConstructor
public class CatalogService {
  private final AcademicService academic;

  public record Route(String slug, String title, String label, String description, List<String> keywords) {
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
  }

  public List<Route> routes() {
    return List.of(
      new Route("investigacion", "Investigación Científica", "Investigación",
        "Metodología, diseño, instrumentos, muestreo y redacción científica.",
        List.of("investigacion","metodologia","diseño","instrumento","muestreo","validacion","redaccion","cientifica","revision sistematica")),
      new Route("tesis", "Tesis y Grados", "Tesis",
        "Pregrado, maestría, doctorado, proyectos y sustentación.",
        List.of("tesis","grado","maestria","doctorado","doctoral","proyecto","sustentacion","bibliografica")),
      new Route("estadistica", "Estadística y Datos", "Estadística",
        "SPSS, R, Python, SEM, correlación, regresión y análisis de datos.",
        List.of("estadistica","datos","spss","python","smartpls","sem","regresion","correlacion","analisis")),
      new Route("ia", "IA para Investigación", "IA",
        "ChatGPT, IA para tesis, revisiones y automatización académica.",
        List.of("ia","inteligencia artificial","chatgpt","prompt","automatizacion")),
      new Route("docencia", "Docencia Universitaria", "Docencia",
        "Sílabos, competencias, ABP, recursos educativos e innovación.",
        List.of("docencia","universitaria","silabo","competencias","abp","pedagogica","educativos")),
      new Route("publicacion", "Publicación Académica", "Publicación",
        "APA 7, Scopus, WoS, OJS, revistas y ética de publicación.",
        List.of("publicacion","apa","scopus","wos","ojs","revista","etica")));
  }

  private String normalized(String value) {
    return Normalizer.normalize(Objects.toString(value, "").toLowerCase(Locale.ROOT),
        Normalizer.Form.NFD).replaceAll("\\p{M}", "");
  }

  private String clean(String value, int max) {
    String result = Objects.toString(value, "").trim();
    if (result.length() > max) throw new BusinessException("El filtro supera el tamaño permitido.");
    return result;
  }

  public Map<String, Object> view(String search, String route, String category, String level, String access) {
    search = clean(search, 180);
    category = clean(category, 100);
    level = clean(level, 60);
    Route selected = routes().stream().filter(r -> r.slug().equals(route)).findFirst().orElse(null);
    String activeRoute = selected == null ? "" : selected.slug();
    String activeAccess = List.of("todos", "inscritos", "disponibles").contains(access) ? access : "todos";
    String needle = normalized(search);
    String cat = category, lvl = level;
    var all = academic.catalog("", "");
    var filtered = all.stream().filter(c -> {
      String haystack = normalized(c.getTitle() + " " + c.getDescription() + " " + c.getCategory() + " " + c.getLevel());
      return (needle.isBlank() || haystack.contains(needle))
          && (cat.isBlank() || c.getCategory().equals(cat))
          && (lvl.isBlank() || c.getLevel().equals(lvl))
          && (selected == null || selected.keywords().stream().anyMatch(k -> haystack.contains(normalized(k))));
    }).toList();
    Account user = academic.current();
    Set<Long> inCart = user != null && user.getRole() == Role.ESTUDIANTE
        ? academic.cart().stream().map(i -> i.getCourse().getId()).collect(java.util.stream.Collectors.toSet())
        : Set.of();
    Map<Long, Map<String, Object>> info = new HashMap<>();
    for (Course c : filtered) {
      boolean enrolled = academic.hasAccess(c, user);
      String status = enrolled ? "inscrito" : inCart.contains(c.getId()) ? "carrito" : "disponible";
      info.put(c.getId(), Map.of("status", status, "modules", academic.modules(c.getId()).size()));
    }
    long enrolledCount = filtered.stream().filter(c -> info.get(c.getId()).get("status").equals("inscrito")).count();
    var result = filtered.stream().filter(c -> activeAccess.equals("todos")
        || (activeAccess.equals("inscritos") == info.get(c.getId()).get("status").equals("inscrito"))).toList();
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("courses", result);
    model.put("courseInfo", info);
    model.put("continueCourses", filtered.stream().filter(c -> info.get(c.getId()).get("status").equals("inscrito")).limit(4).toList());
    model.put("featuredCourses", activeAccess.equals("todos") ? result.stream().limit(4).toList() : List.of());
    model.put("academicRoutes", routes());
    model.put("categories", all.stream().map(Course::getCategory).distinct().sorted().toList());
    model.put("levels", all.stream().map(Course::getLevel).distinct().sorted().toList());
    model.put("search", search); model.put("ruta", activeRoute); model.put("categoria", category);
    model.put("nivel", level); model.put("acceso", activeAccess);
    model.put("accessCounts", Map.of("todos", filtered.size(), "inscritos", enrolledCount, "disponibles", filtered.size() - enrolledCount));
    Map<String, String> params = new LinkedHashMap<>(Map.of("search", search, "ruta", activeRoute, "categoria", category, "nivel", level, "acceso", activeAccess));
    Map<String, String> routeLinks = new HashMap<>();
    for (String slug : List.of("", "investigacion", "tesis", "ia", "docencia", "estadistica", "publicacion")) routeLinks.put(slug, link(params, "ruta", slug));
    Map<String, String> accessLinks = new HashMap<>();
    for (String key : List.of("todos", "inscritos", "disponibles")) accessLinks.put(key, link(params, "acceso", key));
    model.put("routeLinks", routeLinks); model.put("accessLinks", accessLinks);
    return model;
  }

  private String link(Map<String, String> params, String key, String value) {
    var copy = new LinkedHashMap<>(params); copy.put(key, value);
    return "/?" + copy.entrySet().stream().filter(e -> !e.getValue().isBlank())
        .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
        .collect(java.util.stream.Collectors.joining("&"));
  }
}
