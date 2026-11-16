import { test, expect, Page } from "@playwright/test";
import { mkdir, readFile } from "node:fs/promises";
import path from "node:path";
const password = "Inudi2026!Demo";
const evidence = path.resolve("../target/e2e-evidence");
async function login(page: Page, email: string) {
  await page.goto("/login");
  await page.locator("#login-email").fill(email);
  await page.locator("#login-password").fill(password);
  await page
    .getByRole("button", { name: "Iniciar Sesión", exact: true })
    .click();
  await expect(page).not.toHaveURL(/\/login(?:\?|$)/);
}
async function logout(page: Page) {
  await page.locator("#desktop-account-button").click();
  await page
    .locator("#desktop-account-dropdown")
    .getByRole("button", { name: "Cerrar Sesión" })
    .click();
  await expect(page).toHaveURL(/\/login/);
}
async function saveModal(page: Page, name = "Guardar cambios") {
  await page
    .getByRole("dialog")
    .getByRole("button", { name, exact: true })
    .click();
  await expect(page.getByRole("dialog")).not.toBeVisible();
}
async function mobile(page: Page, name: string) {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.locator("#navigation-toggle").click();
  await expect(page.locator("#mobile-account")).toBeVisible();
  await page.locator("#mobile-account-button").click();
  await expect(page.locator("#mobile-account-dropdown")).toBeVisible();
  const account = await page.locator("#mobile-account").boundingBox();
  const menu = await page.locator("#mobile-account-dropdown").boundingBox();
  expect(account!.y + account!.height).toBeLessThanOrEqual(845);
  expect(menu!.y).toBeLessThan(account!.y);
  await page.screenshot({ path: path.join(evidence, name + "-movil.png") });
  await page.keyboard.press("Escape");
  await page.keyboard.press("Escape");
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBe(true);
  await page.setViewportSize({ width: 1280, height: 900 });
}

test("catálogo real: filtros, vacío, fallo de servidor y recuperación", async ({
  page,
}) => {
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "¿Qué deseas lograr hoy?" }),
  ).toBeVisible();
  await page
    .getByRole("searchbox", { name: "Buscar cursos", exact: true })
    .fill("zzzz-no-existe");
  await page.getByRole("button", { name: "Buscar", exact: true }).click();
  await expect(
    page.getByRole("heading", {
      name: "No encontramos cursos con esos criterios",
    }),
  ).toBeVisible();
  await page.getByRole("link", { name: "Ver todo el catálogo" }).click();
  await expect(
    page.getByRole("heading", { name: "Cursos destacados" }),
  ).toBeVisible();
  await mkdir(evidence, { recursive: true });
  await page.screenshot({
    path: path.join(evidence, "catalogo-escritorio.png"),
    fullPage: true,
  });
  await page.route("**/api/cursos*", (route) =>
    route.fulfill({
      status: 503,
      contentType: "application/json",
      body: JSON.stringify({
        message: "Servicio temporalmente no disponible.",
      }),
    }),
  );
  await page.reload();
  await expect(page.getByRole("alert")).toContainText(
    "Servicio temporalmente no disponible.",
  );
  await page.unroute("**/api/cursos*");
  await page.getByRole("button", { name: "Reintentar" }).click();
  await expect(
    page.getByRole("heading", { name: "Cursos destacados" }),
  ).toBeVisible();
  await page.goto("/cursos?search=metodología");
  await expect(page).toHaveURL(/\/?search=metodolog/);
  await expect(page.locator("inudi-card")).toHaveCount(2);
});

test("flujo completo: administración, pago, aprendizaje, docente, evaluación y certificado", async ({
  page,
  context,
  browser,
}) => {
  const runtimeErrors: string[] = [];
  page.on("pageerror", (error) => runtimeErrors.push(error.message));
  await mkdir(evidence, { recursive: true });
  const title = "Curso integrado Angular " + Date.now();
  await test.step("administración y navegación conservada", async () => {
    await login(page, "admin@inudi.local");
    await expect(
      page.getByRole("heading", { name: "Panel de Administración" }),
    ).toBeVisible();
    expect(await page.locator("#sidebar > nav > a").allTextContents()).toEqual([
      "Inicio",
      "Cursos",
      "Usuarios",
      "Pagos",
      "Reseñas",
      "Profesores",
      "Ver como",
      "Sesiones",
    ]);
    await page.screenshot({
      path: path.join(evidence, "administrador-escritorio.png"),
      fullPage: true,
    });
    await mobile(page, "administrador");
  });
  let courseId: number;
  let moduleId: number;
  const contentIds: Record<string, number> = {};
  await test.step("curso y docente en modal sin abandonar el listado", async () => {
    await page
      .locator("#sidebar")
      .getByRole("link", { name: "Cursos", exact: true })
      .click();
    await expect(page).toHaveURL("/admin/cursos");
    const before = page.url();
    await page.getByRole("button", { name: "+ Crear curso" }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    await dialog.getByLabel("Título del curso", { exact: true }).fill(title);
    await dialog
      .getByLabel("Descripción", { exact: true })
      .fill("Curso ficticio para comprobar la integración completa.");
    await dialog
      .getByLabel("Objetivos de aprendizaje", { exact: true })
      .fill("Comprobar flujos y persistencia con Angular y Spring Boot.");
    await dialog.getByLabel("Categoría", { exact: true }).fill("Investigación");
    await dialog.getByLabel("Duración en horas", { exact: true }).fill("16");
    await dialog.getByLabel("Precio en soles", { exact: true }).fill("45.50");
    await dialog.getByLabel("Valeria Mendoza", { exact: true }).check();
    await dialog.getByLabel("Publicar en el catálogo", { exact: true }).check();
    const createdCourse = page.waitForResponse(
      (r) =>
        r.url().endsWith("/api/admin/cursos") &&
        r.request().method() === "POST",
    );
    await saveModal(page);
    expect(page.url()).toBe(before);
    courseId = (await (await createdCourse).json()).id;
    await page
      .locator("article")
      .filter({ hasText: title })
      .getByRole("link", { name: "Gestionar contenidos →" })
      .click();
    await page.getByRole("button", { name: "+ Agregar módulo" }).click();
    await page
      .getByRole("dialog")
      .getByLabel("Nombre del módulo")
      .fill("Módulo de integración");
    const createdModule = page.waitForResponse(
      (r) => r.url().endsWith("/modulos") && r.request().method() === "POST",
    );
    await saveModal(page);
    moduleId = (await (await createdModule).json()).id;
  });
  await test.step("seis tipos de contenido y material privado", async () => {
    for (const type of [
      "TEXTO",
      "VIDEO",
      "MATERIAL",
      "ACTIVIDAD",
      "FORO",
      "EVALUACION",
    ]) {
      await page
        .getByRole("button", { name: "+ Contenido", exact: true })
        .click();
      const d = page.getByRole("dialog");
      await d.getByLabel("Título", { exact: true }).fill("Contenido " + type);
      await d
        .getByRole("combobox", { name: "Tipo de contenido", exact: true })
        .selectOption({ label: type });
      await d
        .getByLabel("Contenido o instrucciones", { exact: true })
        .fill(
          "Instrucciones de " +
            type +
            " <script>window.noDebeEjecutarse=true</script>",
        );
      const created = page.waitForResponse(
        (r) =>
          r.url().endsWith("/gestion/modulos/" + moduleId + "/contenidos") &&
          r.request().method() === "POST",
      );
      await saveModal(page);
      contentIds[type] = (await (await created).json()).id;
    }
    const materialRow = page
      .locator(".syllabus-item")
      .filter({ hasText: "Contenido MATERIAL" });
    await materialRow
      .getByRole("button", { name: "Editar", exact: true })
      .click();
    await page
      .getByRole("dialog")
      .getByLabel("Adjuntar material (opcional, hasta 10 MB)", { exact: true })
      .setInputFiles({
        name: "material.txt",
        mimeType: "text/plain",
        buffer: Buffer.from("Material académico de prueba"),
      });
    await saveModal(page);
  });
  await test.step("cinco tipos de preguntas mediante modal", async () => {
    await page
      .locator(".syllabus-item")
      .filter({ hasText: "Contenido EVALUACION" })
      .getByRole("link", { name: "Preguntas" })
      .click();
    const rows = [
      {
        type: "OPCION_MULTIPLE",
        prompt: "Pregunta de opción",
        options: "A\nB",
        answer: "A",
      },
      {
        type: "VERDADERO_FALSO",
        prompt: "Pregunta de verdad",
        options: "",
        answer: "Verdadero",
      },
      {
        type: "RELACIONAR",
        prompt: "Pregunta de relaciones",
        options: "Problema => Pregunta\nMétodo => Diseño",
        answer: "Pregunta|Diseño",
      },
      {
        type: "COMPLETAR",
        prompt: "Pregunta de completar",
        options: "",
        answer: "evidencia",
      },
      {
        type: "SELECCIONAR_ESPACIOS",
        prompt: "Pregunta de términos",
        options: "teoría\npráctica\ncontexto",
        answer: "teoría|práctica",
      },
    ];
    for (const [i, q] of rows.entries()) {
      await page.getByRole("button", { name: "+ Agregar pregunta" }).click();
      const d = page.getByRole("dialog");
      await d
        .getByRole("combobox", { name: "Tipo", exact: true })
        .selectOption({ label: q.type });
      await d.getByLabel("Enunciado", { exact: true }).fill(q.prompt);
      await d
        .getByLabel("Opciones o pares, uno por línea", { exact: true })
        .fill(q.options);
      await d.getByLabel("Respuesta correcta", { exact: true }).fill(q.answer);
      await d
        .getByLabel("Explicación de la respuesta", { exact: true })
        .fill("Explicación de prueba.");
      await d.getByLabel("Orden", { exact: true }).fill(String(i + 1));
      await saveModal(page);
    }
  });
  await test.step("usuario: error de negocio conserva campos", async () => {
    await page
      .locator("#sidebar")
      .getByRole("link", { name: "Usuarios", exact: true })
      .click();
    await page.getByRole("button", { name: "+ Nuevo usuario" }).click();
    const d = page.getByRole("dialog");
    await d
      .getByLabel("Nombre completo", { exact: true })
      .fill("Usuario de prueba");
    await d
      .getByLabel("Correo electrónico", { exact: true })
      .fill("estudiante@inudi.local");
    await d.getByLabel("Contraseña inicial", { exact: true }).fill(password);
    await d
      .getByRole("button", { name: "Guardar cambios", exact: true })
      .click();
    await expect(d.getByRole("alert")).toContainText(
      "El correo ya está registrado.",
    );
    await expect(d.getByLabel("Nombre completo", { exact: true })).toHaveValue(
      "Usuario de prueba",
    );
    await d
      .getByLabel("Correo electrónico", { exact: true })
      .fill("angular-" + Date.now() + "@inudi.local");
    await saveModal(page);
  });
  await test.step("pago administrativo, edición y matrícula", async () => {
    await page
      .locator("#sidebar")
      .getByRole("link", { name: "Pagos", exact: true })
      .click();
    await page.getByRole("button", { name: "+ Registrar pago" }).click();
    const d = page.getByRole("dialog");
    await d
      .getByRole("combobox", { name: "Estudiante", exact: true })
      .selectOption({ label: "Alex Rivera" });
    await d
      .getByRole("combobox", { name: "Curso", exact: true })
      .selectOption({ label: title });
    await d.getByLabel("Importe en soles", { exact: true }).fill("45.50");
    await d
      .getByLabel("Referencia o sustento", { exact: true })
      .fill("Pago Angular de prueba");
    await saveModal(page, "Registrar pago");
    const row = page.locator("tbody tr").filter({ hasText: title });
    await expect(row).toContainText("CONFIRMADO");
    await row.getByRole("button", { name: "Editar", exact: true }).click();
    await d
      .getByRole("combobox", { name: "Estado", exact: true })
      .selectOption({ label: "PENDIENTE" });
    await d
      .getByRole("button", { name: "Guardar cambios", exact: true })
      .click();
    await expect(d.getByRole("alert")).toBeVisible();
    await expect(
      d.getByLabel("Referencia o sustento", { exact: true }),
    ).toHaveValue("Pago Angular de prueba");
    await d
      .getByRole("combobox", { name: "Estado", exact: true })
      .selectOption({ label: "CONFIRMADO" });
    await d
      .getByLabel("Referencia o sustento", { exact: true })
      .fill("Pago editado desde Angular");
    await saveModal(page);
    await page.screenshot({
      path: path.join(evidence, "pagos-escritorio.png"),
      fullPage: true,
    });
  });
  let submissionId: number;
  await test.step("estudiante: acceso, progreso y archivo", async () => {
    await logout(page);
    await login(page, "estudiante@inudi.local");
    await expect(
      page.getByRole("heading", { name: "Hola, Alex Rivera" }),
    ).toBeVisible();
    await page.screenshot({
      path: path.join(evidence, "estudiante-escritorio.png"),
      fullPage: true,
    });
    await mobile(page, "estudiante");
    await page
      .locator("#sidebar")
      .getByRole("link", { name: "Cursos Inscritos", exact: true })
      .click();
    await expect(
      page.locator("inudi-enrolled article").filter({ hasText: title }),
    ).toBeVisible();
    for (const type of [
      "TEXTO",
      "VIDEO",
      "MATERIAL",
      "ACTIVIDAD",
      "FORO",
      "EVALUACION",
    ]) {
      await page.goto("/contenidos/" + contentIds[type]);
      await expect(
        page.getByRole("heading", { name: "Contenido " + type, exact: true }),
      ).toBeVisible();
      expect(
        await page.evaluate(() =>
          Boolean(
            (window as unknown as Record<string, unknown>)["noDebeEjecutarse"],
          ),
        ),
      ).toBe(false);
      await page
        .getByRole("button", { name: "Marcar como completado ✓" })
        .click();
      await expect(
        page.getByText("✓ Contenido completado", { exact: true }),
      ).toBeVisible();
    }
    await page.goto("/contenidos/" + contentIds["MATERIAL"]);
    const download = page.waitForEvent("download");
    await page.getByRole("link", { name: "Descargar material.txt ↓" }).click();
    expect((await download).suggestedFilename()).toBe("material.txt");
    await page.goto("/contenidos/" + contentIds["ACTIVIDAD"]);
    await page
      .getByRole("button", { name: "Enviar actividad", exact: true })
      .click();
    const d = page.getByRole("dialog");
    await d
      .getByLabel("Archivo de tu actividad", { exact: true })
      .setInputFiles({
        name: "actividad.txt",
        mimeType: "text/plain",
        buffer: Buffer.from("Evidencia del estudiante"),
      });
    await d
      .getByLabel("Comentario para el docente", { exact: true })
      .fill("Entrega realizada desde Angular.");
    await saveModal(page, "Enviar actividad ↑");
    await expect(page.locator("tbody")).toContainText("Por calificar");
    const submissionHref = await page
      .getByRole("link", { name: "actividad.txt ↓" })
      .getAttribute("href");
    submissionId = Number(submissionHref!.match(/entregas\/(\d+)/)![1]);
  });
  await test.step("participación, edición, respuesta y reacción", async () => {
    await page.goto("/contenidos/" + contentIds["FORO"]);
    await page.getByRole("button", { name: "+ Publicar aporte" }).click();
    await page
      .getByRole("dialog")
      .getByLabel("Comparte una idea o pregunta", { exact: true })
      .fill("Aporte académico de Angular.");
    await saveModal(page, "Publicar aporte →");
    const own = page
      .locator(".discussion")
      .filter({ hasText: "Aporte académico de Angular." });
    await own
      .getByRole("button", { name: "Editar mi aporte", exact: true })
      .click();
    await page
      .getByRole("dialog")
      .getByLabel("Contenido", { exact: true })
      .fill("Aporte editado de Angular.");
    await saveModal(page, "Guardar edición");
    const edited = page
      .locator(".discussion")
      .filter({ hasText: "Aporte editado de Angular." });
    await edited
      .getByRole("button", { name: "Responder", exact: true })
      .click();
    await page
      .getByRole("dialog")
      .getByLabel("Tu respuesta", { exact: true })
      .fill("Respuesta de prueba.");
    await saveModal(page, "Publicar respuesta");
    await edited.getByRole("button", { name: /Reaccionar/ }).click();
    await expect(
      edited.getByRole("button", { name: /Reaccionar/ }),
    ).toContainText("1");
  });
  await test.step("evaluación de cinco tipos y resultado protegido", async () => {
    const queried = page.waitForResponse((r) =>
      r.url().endsWith("/api/contenidos/" + contentIds["EVALUACION"]),
    );
    await page.goto("/contenidos/" + contentIds["EVALUACION"]);
    expect(await (await queried).text()).not.toContain("correctAnswer");
    const q = page.locator("fieldset.question");
    await q.nth(0).getByRole("radio", { name: "A", exact: true }).check();
    await q
      .nth(1)
      .getByRole("radio", { name: "Verdadero", exact: true })
      .check();
    await q.nth(2).locator("select").nth(0).selectOption("Pregunta");
    await q.nth(2).locator("select").nth(1).selectOption("Diseño");
    await q.nth(3).locator("input").fill("evidencia");
    await q.nth(4).locator("select").nth(0).selectOption("teoría");
    await q.nth(4).locator("select").nth(1).selectOption("práctica");
    await page.getByRole("button", { name: "Enviar evaluación →" }).click();
    await expect(page).toHaveURL(/evaluaciones\/intentos\/\d+/);
    await expect(page.locator(".result-score")).toContainText("100");
    await expect(page.getByText("APROBADO", { exact: true })).toBeVisible();
    await page.screenshot({
      path: path.join(evidence, "evaluacion-resultado.png"),
      fullPage: true,
    });
  });
  await test.step("docente: seguimiento, calificación y CSV", async () => {
    await logout(page);
    await login(page, "docente@inudi.local");
    await expect(
      page.getByRole("heading", { name: "Menú principal" }),
    ).toBeVisible();
    await page.screenshot({
      path: path.join(evidence, "docente-escritorio.png"),
      fullPage: true,
    });
    await mobile(page, "docente");
    await page.goto("/gestion/cursos/" + courseId + "/seguimiento");
    await page.getByRole("button", { name: "Calificar", exact: true }).click();
    const d = page.getByRole("dialog");
    await d.getByLabel("Nota sobre 20", { exact: true }).fill("18.5");
    await d
      .getByLabel("Retroalimentación", { exact: true })
      .fill("Actividad revisada desde Angular.");
    await saveModal(page);
    await expect(
      page.locator("tbody").filter({ hasText: "18.5" }),
    ).toBeVisible();
    const download = page.waitForEvent("download");
    await page.getByRole("link", { name: "Exportar seguimiento ↓" }).click();
    expect((await download).suggestedFilename()).toBe(
      "seguimiento-" + courseId + ".csv",
    );
    const denied = await page.goto("/api/admin/usuarios");
    expect(denied!.status()).toBe(403);
    await page.goto("/profesor/dashboard");
  });
  let certificateId: number;
  await test.step("estudiante: nota y certificado PDF verificable", async () => {
    await logout(page);
    await login(page, "estudiante@inudi.local");
    await page.goto("/contenidos/" + contentIds["ACTIVIDAD"]);
    await expect(page.locator("tbody")).toContainText("18.5");
    await expect(page.locator("tbody")).toContainText(
      "Actividad revisada desde Angular.",
    );
    await page.goto("/certificados");
    await page
      .locator(".certificate-progress")
      .filter({ hasText: title })
      .getByRole("button", { name: "Obtener certificado" })
      .click();
    const card = page.locator(".certificate-card").filter({ hasText: title });
    await expect(card).toBeVisible();
    const download = page.waitForEvent("download");
    await card.getByRole("link", { name: "Descargar PDF ↓" }).click();
    expect((await download).suggestedFilename()).toMatch(
      /^certificado-inudi-\d{7}\.pdf$/,
    );
    const certificateHref = await card
      .getByRole("link", { name: "Descargar PDF ↓" })
      .getAttribute("href");
    certificateId = Number(certificateHref!.match(/certificados\/(\d+)/)![1]);
    await card.getByRole("link", { name: "Validar ↗" }).click();
    await expect(
      page.getByRole("heading", { name: "Certificado válido" }),
    ).toBeVisible();
    expect(
      (await readFile((await (await download).path())!))
        .subarray(0, 4)
        .toString(),
    ).toBe("%PDF");
  });
  await test.step("otro estudiante no puede descargar evidencias ajenas", async () => {
    const other = await browser.newContext({
      baseURL: "http://127.0.0.1:8093",
    });
    const otherPage = await other.newPage();
    await login(otherPage, "camila@inudi.local");
    expect(
      (await otherPage.goto(
        "/api/entregas/" + submissionId + "/archivo",
      ))!.status(),
    ).toBe(403);
    expect(
      (await otherPage.goto(
        "/api/certificados/" + certificateId + "/pdf",
      ))!.status(),
    ).toBe(403);
    await other.close();
  });
  expect(runtimeErrors).toEqual([]);
});

test("rutas conservadas, filtros, perfil y registro público", async ({
  page,
  context,
}) => {
  const errors: string[] = [];
  page.on("pageerror", (e) => errors.push(e.message));
  await login(page, "admin@inudi.local");
  for (const [url, heading] of [
    ["/admin/user", "Usuarios"],
    ["/admin/profesores", "Profesores"],
    ["/admin/resenas", "Reseñas"],
    ["/admin/sesiones", "Sesiones"],
    ["/admin/vista-previa", "Ver como"],
    ["/admin/matriculas", "Matrículas y vigencia"],
    ["/admin/certificados", "Certificados emitidos"],
    ["/admin/auditoria", "Registro de operaciones"],
  ]) {
    await page.goto(url);
    await expect(
      page.getByRole("heading", { name: heading, exact: true }),
    ).toBeVisible();
    expect(
      await page.evaluate(
        () => document.documentElement.scrollWidth <= window.innerWidth,
      ),
      url,
    ).toBe(true);
  }
  await page.goto("/admin/vista-previa");
  await page.getByRole("button", { name: "Buscar", exact: true }).click();
  await page
    .locator("article")
    .filter({ hasText: "Alex Rivera" })
    .getByRole("link", { name: "Ver como", exact: true })
    .click();
  await expect(
    page.getByText(
      "Vista previa de solo lectura. La sesión de administración se mantiene.",
    ),
  ).toBeVisible();
  await expect(page.locator("#desktop-account-button")).toContainText(
    "Administrador de Inudi",
  );
  await expect(page.locator("#sidebar > nav > a")).toHaveCount(8);
  await page.goto("/admin/sesiones");
  await page
    .getByRole("textbox", { name: "Buscar sesiones" })
    .fill("no-existe-zzzz");
  await page.getByRole("button", { name: "Buscar", exact: true }).click();
  await expect(page.getByText("No hay sesiones registradas.")).toBeVisible();
  await logout(page);
  await login(page, "docente@inudi.local");
  for (const [url, heading] of [
    ["/profesor/cursos", "Mis cursos"],
    ["/profesor/perfil", "Mi perfil"],
    ["/gestion/cursos/1", "Metodología de la investigación científica"],
    ["/aula/cursos/1", "Metodología de la investigación científica"],
  ]) {
    await page.goto(url);
    await expect(
      page.getByRole("heading", { name: heading, exact: true }),
    ).toBeVisible();
  }
  await logout(page);
  await login(page, "estudiante@inudi.local");
  for (const [url, heading] of [
    ["/users/panel/inscritos", "Cursos Inscritos"],
    ["/users/panel/comentarios", "Mis comentarios"],
    ["/users/carrito", "Carrito de compras"],
    ["/mis-pagos", "Mis solicitudes y pagos"],
    ["/users/panel/perfil", "Mi perfil"],
  ]) {
    await page.goto(url);
    await expect(
      page.getByRole("heading", { name: heading, exact: true }),
    ).toBeVisible();
  }
  await page.getByLabel("Teléfono", { exact: true }).fill("999111222");
  await page.getByRole("button", { name: "Guardar cambios" }).click();
  await expect(
    page.getByText("Perfil actualizado.", { exact: true }),
  ).toBeVisible();
  await page.reload();
  await expect(page.getByLabel("Teléfono", { exact: true })).toHaveValue(
    "999111222",
  );
  await page.goto("/admin/user");
  await expect(page).toHaveURL(/acceso-denegado/);
  await expect(
    page.getByRole("heading", { name: "Acceso restringido" }),
  ).toBeVisible();
  await logout(page);
  await page.goto("/registro");
  await page
    .getByLabel("Nombre completo", { exact: true })
    .fill("Estudiante registrado Angular");
  await page
    .getByLabel("Correo electrónico", { exact: true })
    .fill("registro-angular@inudi.local");
  await page
    .getByLabel("Teléfono (opcional)", { exact: true })
    .fill("900111222");
  await page.getByLabel("Contraseña", { exact: true }).fill(password);
  await page.getByRole("button", { name: "Crear cuenta", exact: true }).click();
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole("status")).toContainText(
    "Tu cuenta se creó. Inicia sesión para explorar los cursos.",
  );
  await page.locator("#login-email").fill("registro-angular@inudi.local");
  await page.locator("#login-password").fill(password);
  await page
    .getByRole("button", { name: "Iniciar Sesión", exact: true })
    .click();
  await expect(
    page.getByRole("heading", { name: "Hola, Estudiante registrado Angular" }),
  ).toBeVisible();
  expect(errors).toEqual([]);
});

test("carrito y solicitud pendiente conservan la validación administrativa", async ({
  page,
}) => {
  await login(page, "camila@inudi.local");
  await page.goto("/cursos/3");
  await page
    .getByRole("button", { name: "Agregar a mi selección", exact: true })
    .click();
  await expect(
    page.getByRole("heading", { name: "Carrito de compras" }),
  ).toBeVisible();
  await expect(page.getByRole("status")).toContainText(
    "Curso agregado a tu selección.",
  );
  await expect(page.locator(".cart-row")).toContainText(
    "Herramientas digitales para la investigación",
  );
  await page
    .getByRole("button", { name: "Solicitar matrícula →", exact: true })
    .click();
  await expect(page).toHaveURL(/\/mis-pagos$/);
  await expect(page.getByRole("status")).toContainText(
    "La administración validará el pago y habilitará tu matrícula.",
  );
  const row = page
    .locator("tbody tr")
    .filter({ hasText: "Herramientas digitales para la investigación" });
  await expect(row).toContainText("PENDIENTE");
  await page.screenshot({
    path: path.join(evidence, "solicitud-pendiente.png"),
    fullPage: true,
  });
  expect((await page.goto("/api/aula/cursos/3"))!.status()).toBe(403);
});
