import { expect, Page, test } from "@playwright/test";

async function save(page: Page): Promise<void> {
  const dialog = page.getByRole("dialog");
  await dialog
    .getByRole("button", { name: "Guardar cambios", exact: true })
    .click();
  await expect(dialog).not.toBeVisible();
}

test("edición persistente y eliminación de curso, módulo, contenido y pregunta", async ({
  page,
}) => {
  const errors: string[] = [];
  page.on("pageerror", (error) => errors.push(error.message));
  page.on("dialog", (dialog) => void dialog.accept());
  await page.goto("/login");
  await page.locator("#login-email").fill("admin@inudi.local");
  await page.locator("#login-password").fill("Inudi2026!Demo");
  await page
    .getByRole("button", { name: "Iniciar Sesión", exact: true })
    .click();
  await expect(page).toHaveURL("/dashboard");
  await page.goto("/admin/cursos");
  await page.getByRole("button", { name: "+ Crear curso" }).click();
  const dialog = page.getByRole("dialog");
  const title = "Curso temporal de revisión CRUD";
  await dialog.getByLabel("Título del curso", { exact: true }).fill(title);
  await dialog
    .getByLabel("Descripción", { exact: true })
    .fill("Datos ficticios aislados para verificar edición y eliminación.");
  await dialog
    .getByLabel("Objetivos de aprendizaje", { exact: true })
    .fill("Verificar persistencia sin alterar cursos existentes.");
  await dialog.getByLabel("Categoría", { exact: true }).fill("Verificación");
  await save(page);
  const card = page.locator("article").filter({ hasText: title });
  await card.getByRole("button", { name: "Editar datos del curso" }).click();
  await dialog
    .getByLabel("Título del curso", { exact: true })
    .fill(title + " actualizado");
  await dialog.getByLabel("Precio en soles", { exact: true }).fill("34.90");
  await save(page);
  await page.reload();
  await card.getByRole("button", { name: "Editar datos del curso" }).click();
  await expect(
    dialog.getByLabel("Título del curso", { exact: true }),
  ).toHaveValue(title + " actualizado");
  await expect(
    dialog.getByLabel("Precio en soles", { exact: true }),
  ).toHaveValue("34.9");
  await page.keyboard.press("Escape");
  const managementLink = card.getByRole("link", {
    name: "Gestionar contenidos →",
  });
  const coursePath = await managementLink.getAttribute("href");
  const courseId = Number(coursePath!.split("/").at(-1));
  await managementLink.click();
  await expect(page).toHaveURL(coursePath!);
  await page.getByRole("button", { name: "+ Agregar módulo" }).click();
  await dialog.getByLabel("Nombre del módulo").fill("Módulo temporal");
  await save(page);
  await page
    .getByRole("button", { name: "Editar módulo", exact: true })
    .click();
  await dialog.getByLabel("Nombre del módulo").fill("Módulo actualizado");
  await save(page);
  await page.reload();
  await expect(
    page.getByRole("heading", { name: "Módulo actualizado", exact: true }),
  ).toBeVisible();
  await page
    .getByRole("button", { name: "Eliminar curso", exact: true })
    .click();
  await expect(page.getByRole("alert")).toBeVisible();
  await expect(page).toHaveURL("/gestion/cursos/" + courseId);
  await page.getByRole("button", { name: "+ Contenido", exact: true }).click();
  await dialog
    .getByLabel("Título", { exact: true })
    .fill("Evaluación temporal");
  await dialog
    .getByRole("combobox", { name: "Tipo de contenido", exact: true })
    .selectOption({ label: "EVALUACION" });
  await save(page);
  await page
    .locator(".syllabus-item")
    .getByRole("button", { name: "Editar", exact: true })
    .click();
  await dialog
    .getByLabel("Título", { exact: true })
    .fill("Evaluación actualizada");
  await dialog
    .getByLabel("Contenido o instrucciones", { exact: true })
    .fill("Instrucciones actualizadas y persistidas.");
  await save(page);
  await page.reload();
  await expect(page.locator(".syllabus-item")).toContainText(
    "Evaluación actualizada",
  );
  await page.getByRole("link", { name: "Preguntas", exact: true }).click();
  await page
    .getByRole("button", { name: "+ Agregar pregunta", exact: true })
    .click();
  await dialog
    .getByRole("combobox", { name: "Tipo", exact: true })
    .selectOption({ label: "COMPLETAR" });
  await dialog
    .getByLabel("Enunciado", { exact: true })
    .fill("Pregunta temporal");
  await dialog
    .getByLabel("Respuesta correcta", { exact: true })
    .fill("persistencia");
  await save(page);
  await page
    .locator("article")
    .filter({ hasText: "Pregunta temporal" })
    .getByRole("button", { name: "Editar", exact: true })
    .click();
  await dialog
    .getByLabel("Enunciado", { exact: true })
    .fill("Pregunta actualizada");
  await save(page);
  await page.reload();
  const question = page
    .locator("article")
    .filter({ hasText: "Pregunta actualizada" });
  await expect(question).toBeVisible();
  await question.getByRole("button", { name: "Eliminar", exact: true }).click();
  await expect(question).toHaveCount(0);
  await page
    .getByRole("link", { name: "← Gestión del curso", exact: true })
    .click();
  await page
    .locator(".syllabus-item")
    .getByRole("button", { name: "Eliminar", exact: true })
    .click();
  await expect(page.locator(".syllabus-item")).toHaveCount(0);
  await page
    .getByRole("button", { name: "Eliminar módulo vacío", exact: true })
    .click();
  await expect(
    page.getByRole("heading", { name: "Módulo actualizado", exact: true }),
  ).toHaveCount(0);
  await page
    .getByRole("button", { name: "Eliminar curso", exact: true })
    .click();
  await expect(page).toHaveURL("/admin/cursos");
  await expect(card).toHaveCount(0);
  await page.reload();
  await expect(card).toHaveCount(0);
  expect((await page.goto("/api/cursos/" + courseId))!.status()).toBe(404);
  expect(errors).toEqual([]);
});
