import { Component, signal, ViewChild } from "@angular/core";
import { DecimalPipe } from "@angular/common";
import { RouterLink } from "@angular/router";
import { AsyncPage } from "../shared/async-page";
import { StatusComponent } from "../shared/status";
import { SyllabusComponent } from "../shared/syllabus";
import { EditorComponent } from "../shared/editor";
import { Classroom, CourseDetail } from "../core/models";
@Component({
  standalone: true,
  imports: [
    DecimalPipe,
    RouterLink,
    StatusComponent,
    SyllabusComponent,
    EditorComponent,
  ],
  template: `
    <inudi-status
      [loading]="loading()"
      [error]="error()"
      [notice]="notice()"
      (retry)="refresh()"
    />
    @if (data(); as d) {
      <a class="back-link" routerLink="/"
        >← {{ classroom ? "Cursos" : "Programas formativos" }}</a
      >
      @if (classroom) {
        <div class="page-heading">
          <h1>{{ d.course.title }}</h1>
          <p>
            {{ d.course.teacherNames }} · {{ d.course.durationHours }} horas
          </p>
        </div>
        <div class="two-column">
          <section class="panel">
            <h2>Contenido del curso</h2>
            <inudi-syllabus
              [rows]="d.syllabus"
              [access]="true"
              [completed]="completed()"
            />
          </section>
          <aside class="panel">
            <span class="stat-icon mint"
              ><svg><use href="#i-chart" /></svg
            ></span>
            <h2>{{ d.canManage ? "Vista del aula" : "Cada paso cuenta" }}</h2>
            @if (d.canManage) {
              <p>Consulta los contenidos tal como los ve el estudiante.</p>
              <a class="button" [routerLink]="['/gestion/cursos', d.course.id]"
                >Gestionar contenidos</a
              >
            } @else {
              <div class="progress-label">
                <span>Tu progreso</span><strong>{{ percent() }}%</strong>
              </div>
              <progress max="100" [value]="percent()"></progress>
              <p class="muted">
                Completa las lecciones y conserva tus evidencias de aprendizaje.
              </p>
              <a class="text-link" routerLink="/certificados"
                >Ver mis logros ↗</a
              >
            }
          </aside>
        </div>
      } @else {
        <div class="course-detail-grid">
          <div>
            <div [class]="'course-detail-hero tone-' + (d.course.id % 3)">
              <span class="pill">{{ d.course.category }}</span>
              <h1>{{ d.course.title }}</h1>
              <p>
                {{ d.course.durationHours }} horas · {{ d.course.level }} ·
                Formación en línea
              </p>
              <span class="detail-orbit"></span>
            </div>
            <section class="panel section-space">
              <h2>Descripción del curso</h2>
              <p class="preserve-lines">{{ d.course.description }}</p>
              <h3>Objetivos de aprendizaje</h3>
              <p class="preserve-lines">{{ d.course.objectives }}</p>
              <h3>Equipo docente</h3>
              <p>{{ d.course.teacherNames || "Sin asignación docente" }}</p>
            </section>
            <section class="panel">
              <h2>Contenido del curso</h2>
              <inudi-syllabus [rows]="d.syllabus" [access]="d.canLearn" />
            </section>
          </div>
          <aside class="panel course-purchase">
            <span class="eyebrow">INVERSIÓN EN TU FORMACIÓN</span>
            <div class="course-price">
              S/ {{ d.course.price | number: "1.2-2" }}
            </div>
            <p class="muted">
              Acceso sujeto a validación administrativa de la matrícula.
            </p>
            @if (d.canLearn) {
              <a
                class="button button-full"
                [routerLink]="['/aula/cursos', d.course.id]"
                >Ir al aula →</a
              >
            } @else if (auth.user()?.role === "ESTUDIANTE") {
              <button
                class="button button-full"
                type="button"
                [disabled]="saving()"
                (click)="add(d.course.id)"
              >
                Agregar a mi selección
              </button>
            } @else if (!auth.user()) {
              <a class="button button-full" routerLink="/login"
                >Ingresar para inscribirme</a
              >
            } @else {
              <p>Tu perfil de gestión no realiza compras.</p>
            }
            <ul class="feature-list">
              <li>✓ {{ d.course.durationHours }} horas de formación</li>
              <li>✓ Materiales y actividades</li>
              <li>✓ Evaluaciones formativas</li>
              <li>✓ Participación académica</li>
              @if (d.course.certificateEnabled) {
                <li>✓ Certificado al completar el curso</li>
              }
            </ul>
            <div class="soft-note">Tu aprendizaje, en un solo lugar.</div>
          </aside>
        </div>
        <section class="panel section-space" id="resenas">
          <span class="eyebrow">COMUNIDAD ACADÉMICA</span>
          <h2>Experiencias del curso</h2>
          @for (review of d.reviews; track review.id) {
            <article class="review">
              <strong>{{ review.author.fullName }}</strong
              ><span class="badge">★ {{ review.score }}/5</span>
              <p>{{ review.body }}</p>
              @if (
                auth.user()?.id === review.author.id ||
                auth.user()?.role === "ADMIN"
              ) {
                <button
                  type="button"
                  class="link-button danger"
                  [disabled]="saving()"
                  (click)="removeReview(review.id)"
                >
                  Retirar reseña
                </button>
              }
            </article>
          }
          @if (!d.reviews.length) {
            <p class="muted">Aún no se han compartido experiencias.</p>
          }
          @if (auth.user()?.role === "ESTUDIANTE" && d.canLearn) {
            <button type="button" class="button" (click)="review()">
              Guardar mi reseña
            </button>
          }
        </section>
      }
    }
    <inudi-editor />
  `,
})
export class CourseComponent extends AsyncPage {
  @ViewChild(EditorComponent) editor!: EditorComponent;
  classroom = this.route.snapshot.data["view"] === "classroom";
  data = signal<CourseDetail | null>(null);
  completed = signal<number[]>([]);
  percent = signal(0);
  constructor() {
    super();
    this.watch(() => this.refresh());
  }
  async refresh(): Promise<void> {
    await this.load(async () => {
      const id = this.route.snapshot.paramMap.get("id");
      if (this.classroom) {
        const d = await this.api.get<Classroom>("/aula/cursos/" + id);
        this.data.set({ ...d, canLearn: true, reviews: [] });
        this.completed.set(d.completed);
        this.percent.set(d.percent);
      } else this.data.set(await this.api.get<CourseDetail>("/cursos/" + id));
    });
  }
  async add(id: number): Promise<void> {
    await this.action(
      async () => {
        await this.api.send("POST", "/carrito/" + id);
        await this.auth.refresh();
        await this.router.navigate(["/carrito"]);
      },
      async () => {},
      "Curso agregado a tu selección.",
    );
  }
  review(): void {
    this.editor.open({
      title: "Comparte tu experiencia",
      fields: [
        {
          name: "score",
          label: "Tu valoración",
          type: "select",
          required: true,
          options: [1, 2, 3, 4, 5].map((value) => ({
            value,
            label: value + " de 5",
          })),
        },
        {
          name: "body",
          label: "Comparte tu experiencia",
          type: "textarea",
          required: true,
          maxLength: 2000,
        },
      ],
      initial: { score: 5, body: "" },
      save: async (value) => {
        await this.api.send(
          "POST",
          "/cursos/" + this.data()!.course.id + "/resenas",
          value,
        );
        await this.refresh();
      },
    });
  }
  async removeReview(id: number): Promise<void> {
    await this.action(
      () => this.api.send("DELETE", "/resenas/" + id),
      () => this.refresh(),
      "Reseña retirada.",
    );
  }
}
