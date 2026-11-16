import { Component, signal, ViewChild } from "@angular/core";
import { RouterLink } from "@angular/router";
import { AsyncPage } from "../shared/async-page";
import { StatusComponent } from "../shared/status";
import { EditorComponent } from "../shared/editor";
import {
  courseFields,
  contentFields,
  moduleFields,
  questionFields,
} from "../shared/forms";
import {
  AcademicModule,
  Content,
  Course,
  Options,
  Question,
  Report,
  Submission,
  SyllabusRow,
} from "../core/models";
@Component({
  standalone: true,
  imports: [RouterLink, StatusComponent, EditorComponent],
  template: `
    <inudi-status
      [loading]="loading()"
      [error]="error()"
      [notice]="notice()"
      (retry)="refresh()"
    />
    @switch (view === "contentEditor" ? "manage" : view) {
      @case ("list") {
        <div class="page-heading with-action">
          <h1>{{ auth.user()?.role === "ADMIN" ? "Cursos" : "Mis cursos" }}</h1>
          @if (auth.user()?.role === "ADMIN") {
            <button type="button" class="button" (click)="editCourse()">
              + Crear curso
            </button>
          }
        </div>
        <div class="management-grid">
          @for (c of courses(); track c.id) {
            <article class="panel management-card">
              <div class="management-card-top">
                <span class="stat-icon"
                  ><svg><use href="#i-book" /></svg></span
                ><span
                  class="badge"
                  [class.positive]="c.published"
                  [class.warning]="!c.published"
                  >{{ c.published ? "PUBLICADO" : "BORRADOR" }}</span
                >
              </div>
              <span class="eyebrow">{{ c.category }}</span>
              <h2>{{ c.title }}</h2>
              <p class="muted small">{{ c.teacherNames }}</p>
              <div class="management-card-actions">
                <a
                  class="button button-small"
                  [routerLink]="['/gestion/cursos', c.id]"
                  >Gestionar contenidos →</a
                ><a
                  class="text-link"
                  [routerLink]="['/gestion/cursos', c.id, 'seguimiento']"
                  >Seguimiento académico ↗</a
                >
                @if (auth.user()?.role === "ADMIN") {
                  <button
                    type="button"
                    class="link-button text-link"
                    (click)="editCourse(c)"
                  >
                    Editar datos del curso
                  </button>
                }
              </div>
            </article>
          }
        </div>
        @if (!courses().length && !loading()) {
          <div class="empty-state">
            <h2>No hay cursos asignados</h2>
            <p>
              Los cursos aparecen cuando la administración los registra y asigna
              al docente.
            </p>
          </div>
        }
      }
      @case ("manage") {
        @if (detail(); as d) {
          <a class="back-link" routerLink="/gestion/cursos"
            >← Gestión de cursos</a
          >
          <div class="page-heading">
            <span class="eyebrow">ORGANIZACIÓN DEL APRENDIZAJE</span>
            <h1>{{ d.course.title }}</h1>
            <div class="actions">
              <a
                class="button button-ghost"
                [routerLink]="['/aula/cursos', d.course.id]"
                >Vista del aula ↗</a
              ><a
                class="button"
                [routerLink]="['/gestion/cursos', d.course.id, 'seguimiento']"
                >Seguimiento académico →</a
              >
              @if (auth.user()?.role === "ADMIN") {
                <button
                  type="button"
                  class="link-button text-link"
                  (click)="editCourse(d.course)"
                >
                  Editar curso
                </button>
              }
            </div>
          </div>
          @for (row of d.syllabus; track row.module.id) {
            <section class="panel section-space">
              <div class="section-heading">
                <div>
                  <span class="eyebrow">MÓDULO {{ row.module.position }}</span>
                  <h2>{{ row.module.title }}</h2>
                </div>
                <button
                  type="button"
                  class="button button-small"
                  (click)="editContent(row.module)"
                >
                  + Contenido
                </button>
              </div>
              @for (c of row.contents; track c.id) {
                <div class="syllabus-item">
                  <span class="lesson-icon">▤</span>
                  <div>
                    <strong>{{ c.title }}</strong
                    ><small
                      >{{ c.type }} · Orden {{ c.position }}
                      {{ c.preview ? "· Vista previa" : "" }}</small
                    >
                  </div>
                  <div class="row-actions">
                    <button
                      type="button"
                      class="link-button text-link"
                      (click)="editContent(row.module, c)"
                    >
                      Editar
                    </button>
                    @if (c.type === "EVALUACION") {
                      <a
                        class="text-link"
                        [routerLink]="[
                          '/gestion/contenidos',
                          c.id,
                          'preguntas',
                        ]"
                        >Preguntas</a
                      >
                    }
                    <button
                      type="button"
                      class="link-button danger"
                      [disabled]="saving()"
                      (click)="
                        remove(
                          '/gestion/contenidos/' + c.id,
                          '¿Eliminar este contenido sin historial académico?'
                        )
                      "
                    >
                      Eliminar
                    </button>
                  </div>
                </div>
              }
              <div class="actions section-space">
                <button
                  type="button"
                  class="button button-ghost"
                  (click)="editModule(row.module)"
                >
                  Editar módulo</button
                ><button
                  type="button"
                  class="link-button danger"
                  [disabled]="saving()"
                  (click)="
                    remove(
                      '/gestion/modulos/' + row.module.id,
                      '¿Eliminar este módulo vacío?'
                    )
                  "
                >
                  Eliminar módulo vacío
                </button>
              </div>
            </section>
          }
          <div class="section-space">
            <button type="button" class="button" (click)="editModule()">
              + Agregar módulo
            </button>
          </div>
          @if (auth.user()?.role === "ADMIN") {
            <section class="panel section-space">
              <h3>Eliminar un curso sin registros</h3>
              <p class="muted small">
                Solo puede eliminarse un curso vacío y sin relaciones académicas
                o comerciales. Un curso con historial puede ocultarse del
                catálogo desde su edición.
              </p>
              <button
                class="button button-danger"
                [disabled]="saving()"
                (click)="deleteCourse(d.course.id)"
              >
                Eliminar curso
              </button>
            </section>
          }
        }
      }
      @case ("questions") {
        @if (questionData(); as d) {
          <a
            class="back-link"
            [routerLink]="['/gestion/cursos', d.content.courseId]"
            >← Gestión del curso</a
          >
          <div class="page-heading">
            <span class="eyebrow">EVALUACIÓN FORMATIVA</span>
            <h1>Cuestionario</h1>
            <p>{{ d.content.title }}</p>
          </div>
          <section class="panel">
            <div class="section-heading">
              <h2>Preguntas registradas</h2>
              <button type="button" class="button" (click)="editQuestion()">
                + Agregar pregunta
              </button>
            </div>
            @for (q of d.questions; track q.id) {
              <article class="question-management">
                <span class="badge">{{ q.type }} · {{ q.points }} puntos</span>
                <h3>{{ q.position }}. {{ q.prompt }}</h3>
                <p class="muted small">Respuesta: {{ q.correctAnswer }}</p>
                <div class="row-actions">
                  <button
                    type="button"
                    class="link-button text-link"
                    (click)="editQuestion(q)"
                  >
                    Editar</button
                  ><button
                    type="button"
                    class="link-button danger"
                    [disabled]="saving()"
                    (click)="
                      remove(
                        '/gestion/preguntas/' + q.id,
                        '¿Eliminar esta pregunta sin intentos?'
                      )
                    "
                  >
                    Eliminar
                  </button>
                </div>
              </article>
            }
            @if (!d.questions.length) {
              <p class="muted">
                Añade preguntas para habilitar esta evaluación.
              </p>
            }
            <div class="soft-note">
              El cuestionario queda protegido frente a modificaciones cuando
              registra su primer intento.
            </div>
          </section>
        }
      }
      @case ("report") {
        @if (report(); as d) {
          <a class="back-link" [routerLink]="['/gestion/cursos', d.course.id]"
            >← Gestión del curso</a
          >
          <div class="page-heading with-action">
            <div>
              <span class="eyebrow">ACOMPAÑAMIENTO CON EVIDENCIAS</span>
              <h1>Seguimiento académico</h1>
              <p>{{ d.course.title }}</p>
            </div>
            <a
              class="button button-ghost"
              [href]="'/api/gestion/cursos/' + d.course.id + '/reporte.csv'"
              >Exportar seguimiento ↓</a
            >
          </div>
          <section class="panel">
            <h2>Estudiantes y progreso</h2>
            <div
              class="table-wrap"
              tabindex="0"
              role="region"
              aria-label="Estudiantes y progreso"
            >
              <table>
                <thead>
                  <tr>
                    <th>Participante</th>
                    <th>Correo</th>
                    <th>Progreso</th>
                    <th>Vigencia</th>
                  </tr>
                </thead>
                <tbody>
                  @for (row of d.roster; track row.enrollment.id) {
                    <tr>
                      <td>
                        <div class="person-cell">
                          <span class="avatar">{{ row.student.initials }}</span
                          ><strong>{{ row.student.fullName }}</strong>
                        </div>
                      </td>
                      <td>{{ row.student.email }}</td>
                      <td>
                        <progress max="100" [value]="row.percent"></progress
                        ><small>{{ row.percent }}%</small>
                      </td>
                      <td>
                        <span
                          class="badge"
                          [class.positive]="row.enrollment.current"
                          [class.warning]="!row.enrollment.current"
                          >{{
                            row.enrollment.current ? "VIGENTE" : "NO VIGENTE"
                          }}</span
                        >
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            @if (!d.roster.length) {
              <p class="muted">
                El curso aún no tiene estudiantes matriculados.
              </p>
            }
          </section>
          <section class="panel section-space">
            <h2>Actividades y calificaciones</h2>
            <div
              class="table-wrap"
              tabindex="0"
              role="region"
              aria-label="Actividades y calificaciones"
            >
              <table>
                <thead>
                  <tr>
                    <th>Estudiante / actividad</th>
                    <th>Entrega</th>
                    <th>Calificación</th>
                  </tr>
                </thead>
                <tbody>
                  @for (s of d.submissions; track s.id) {
                    <tr>
                      <td>
                        <strong>{{ s.student.fullName }}</strong
                        ><small
                          >{{ s.content.title }} · Intento
                          {{ s.attemptNumber }}</small
                        >
                      </td>
                      <td>
                        <a
                          class="text-link"
                          [href]="'/api/entregas/' + s.id + '/archivo'"
                          >{{ s.fileName }} ↓</a
                        ><small>{{ s.comment }}</small>
                      </td>
                      <td>
                        <p>
                          {{ s.grade === null ? "Por calificar" : s.grade }}
                        </p>
                        <button
                          type="button"
                          class="button button-small"
                          (click)="grade(s)"
                        >
                          {{ s.grade === null ? "Calificar" : "Editar nota" }}
                        </button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            @if (!d.submissions.length) {
              <p class="muted">Aún no se han entregado actividades.</p>
            }
          </section>
          <section class="panel section-space">
            <h2>Resultados de evaluaciones</h2>
            <div
              class="table-wrap"
              tabindex="0"
              role="region"
              aria-label="Resultados de evaluaciones"
            >
              <table>
                <thead>
                  <tr>
                    <th>Estudiante</th>
                    <th>Evaluación</th>
                    <th>Intento</th>
                    <th>Resultado</th>
                    <th>Estado</th>
                    <th>Detalle</th>
                  </tr>
                </thead>
                <tbody>
                  @for (a of d.attempts; track a.id) {
                    <tr>
                      <td>{{ a.student.fullName }}</td>
                      <td>{{ a.content.title }}</td>
                      <td>{{ a.attemptNumber }}</td>
                      <td>{{ a.percent }}% · {{ a.stars }} ★</td>
                      <td>
                        <span
                          class="badge"
                          [class.positive]="a.passed"
                          [class.warning]="!a.passed"
                          >{{ a.passed ? "APROBADO" : "NO APROBADO" }}</span
                        >
                      </td>
                      <td>
                        <a
                          class="text-link"
                          [routerLink]="['/evaluaciones/intentos', a.id]"
                          >Ver →</a
                        >
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            @if (!d.attempts.length) {
              <p class="muted">Aún no hay evaluaciones resueltas.</p>
            }
          </section>
        }
      }
    }
    <inudi-editor />
  `,
})
export class ManagementComponent extends AsyncPage {
  @ViewChild(EditorComponent) editor!: EditorComponent;
  view = this.route.snapshot.data["view"] as string;
  courses = signal<Course[]>([]);
  detail = signal<{ course: Course; syllabus: SyllabusRow[] } | null>(null);
  questionData = signal<{ content: Content; questions: Question[] } | null>(
    null,
  );
  report = signal<Report | null>(null);
  constructor() {
    super();
    this.watch(() => this.refresh());
  }
  async refresh(): Promise<void> {
    await this.load(async () => {
      const id = this.route.snapshot.paramMap.get("id");
      if (this.view === "list") {
        const d = await this.api.get<{ courses: Course[] }>("/gestion/cursos");
        this.courses.set(d.courses);
        if (this.route.snapshot.data["editor"] === "course") {
          const c = id ? d.courses.find((c) => c.id === Number(id)) : undefined;
          if (id && !c) throw new Error("El curso no existe.");
          await this.editCourse(c);
        }
      } else if (this.view === "manage")
        this.detail.set(await this.api.get("/gestion/cursos/" + id));
      else if (this.view === "contentEditor") {
        const content = id
          ? (await this.api.get<{ content: Content }>("/contenidos/" + id))
              .content
          : undefined;
        const module = await this.api.get<AcademicModule>(
          "/gestion/modulos/" +
            (content?.moduleId || this.route.snapshot.paramMap.get("moduleId")),
        );
        this.detail.set(
          await this.api.get("/gestion/cursos/" + module.courseId),
        );
        await this.editContent(module, content);
      } else if (this.view === "questions") {
        this.questionData.set(
          await this.api.get("/gestion/contenidos/" + id + "/preguntas"),
        );
        const edit = this.route.snapshot.queryParamMap.get("editar");
        if (edit)
          this.editQuestion(
            this.questionData()!.questions.find((q) => q.id === Number(edit)),
          );
      } else if (this.view === "report")
        this.report.set(
          await this.api.get<Report>("/gestion/cursos/" + id + "/seguimiento"),
        );
    });
  }
  async editCourse(c?: Course): Promise<void> {
    try {
      const o = await this.api.get<Options>("/admin/opciones");
      this.editor.open({
        title: c ? "Editar curso" : "Crear curso",
        fields: courseFields(o),
        initial: c
          ? { ...c }
          : {
              level: "Inicial",
              durationHours: 12,
              price: 0,
              teacherIds: [],
              published: false,
              certificateEnabled: true,
            },
        save: async (value) => {
          await this.api.send(
            c ? "PUT" : "POST",
            "/admin/cursos" + (c ? "/" + c.id : ""),
            value,
          );
          this.notice.set("Curso guardado.");
          if (this.route.snapshot.data["editor"])
            await this.router.navigate(["/admin/cursos"]);
          else await this.refresh();
        },
      });
    } catch (e) {
      await this.load(async () => {
        throw e;
      });
    }
  }
  editModule(m?: AcademicModule): void {
    const d = this.detail()!;
    this.editor.open({
      title: m ? "Editar módulo" : "Agregar módulo",
      fields: moduleFields,
      initial: m ? { ...m } : { title: "", position: d.syllabus.length + 1 },
      save: async (value) => {
        await this.api.send(
          m ? "PUT" : "POST",
          "/gestion/cursos/" + d.course.id + "/modulos" + (m ? "/" + m.id : ""),
          value,
        );
        this.notice.set("Módulo guardado.");
        await this.refresh();
      },
    });
  }
  async editContent(module: AcademicModule, c?: Content): Promise<void> {
    try {
      const initial = c
        ? (await this.api.get<{ content: Content }>("/contenidos/" + c.id))
            .content
        : undefined;
      this.editor.open({
        title: c ? "Editar contenido" : "Nuevo contenido",
        fields: c
          ? [
              ...contentFields,
              {
                name: "file",
                label: "Adjuntar material (opcional, hasta 10 MB)",
                type: "file",
              },
            ]
          : contentFields,
        initial: initial
          ? { ...initial }
          : {
              title: "",
              type: "TEXTO",
              position:
                (this.detail()?.syllabus.find((r) => r.module.id === module.id)
                  ?.contents.length || 0) + 1,
              body: "",
              resourceUrl: "",
              maxAttempts: 3,
              passPercent: 70,
              examActive: true,
              preview: false,
            },
        save: async (value) => {
          const { file, ...body } = value;
          const saved = await this.api.send<Content>(
            c ? "PUT" : "POST",
            "/gestion/modulos/" +
              module.id +
              "/contenidos" +
              (c ? "/" + c.id : ""),
            body,
          );
          if (file instanceof File)
            await this.api.upload(
              "/gestion/contenidos/" + saved.id + "/material",
              file,
            );
          this.notice.set("Contenido guardado.");
          if (this.view === "contentEditor")
            await this.router.navigate(["/gestion/cursos", module.courseId]);
          else await this.refresh();
        },
      });
    } catch (e) {
      await this.load(async () => {
        throw e;
      });
    }
  }
  editQuestion(q?: Question): void {
    const d = this.questionData()!;
    this.editor.open({
      title: q ? "Editar pregunta" : "Agregar pregunta",
      fields: questionFields,
      initial: q
        ? { ...q }
        : {
            type: "OPCION_MULTIPLE",
            prompt: "",
            optionsText: "",
            correctAnswer: "",
            explanation: "",
            points: 1,
            position: d.questions.length + 1,
          },
      save: async (value) => {
        await this.api.send(
          q ? "PUT" : "POST",
          "/gestion/contenidos/" +
            d.content.id +
            "/preguntas" +
            (q ? "/" + q.id : ""),
          value,
        );
        this.notice.set("Pregunta guardada.");
        if (this.route.snapshot.queryParamMap.has("editar"))
          await this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {},
          });
        else await this.refresh();
      },
    });
  }
  grade(s: Submission): void {
    this.editor.open({
      title: "Calificar actividad",
      fields: [
        {
          name: "grade",
          label: "Nota sobre 20",
          type: "number",
          required: true,
          min: 0,
          max: 20,
          step: "0.01",
        },
        {
          name: "feedback",
          label: "Retroalimentación",
          type: "textarea",
          maxLength: 1000,
        },
      ],
      initial: { grade: s.grade, feedback: s.feedback || "" },
      save: async (value) => {
        await this.api.send(
          "PUT",
          "/gestion/entregas/" + s.id + "/calificar",
          value,
        );
        this.notice.set("Calificación y retroalimentación guardadas.");
        await this.refresh();
      },
    });
  }
  async remove(path: string, message: string): Promise<void> {
    if (!window.confirm(message)) return;
    await this.action(
      () => this.api.send("DELETE", path),
      () => this.refresh(),
      "Registro eliminado.",
    );
  }
  async deleteCourse(id: number): Promise<void> {
    if (!window.confirm("¿Eliminar definitivamente este curso vacío?")) return;
    await this.action(
      async () => {
        await this.api.send("DELETE", "/admin/cursos/" + id);
        await this.router.navigate(["/admin/cursos"]);
      },
      async () => {},
      "Curso eliminado.",
    );
  }
}
