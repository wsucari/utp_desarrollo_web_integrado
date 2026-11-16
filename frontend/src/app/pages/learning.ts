import { Component, inject, signal, ViewChild } from "@angular/core";
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { RouterLink } from "@angular/router";
import { AsyncPage } from "../shared/async-page";
import { StatusComponent } from "../shared/status";
import { EditorComponent } from "../shared/editor";
import { Answer, Attempt, Lesson, Post, Question } from "../core/models";

@Component({
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    StatusComponent,
    EditorComponent,
  ],
  template: `
    <inudi-status
      [loading]="loading()"
      [error]="error()"
      [notice]="notice()"
      (retry)="refresh()"
    />
    @if (result(); as d) {
      <a class="back-link" [routerLink]="['/contenidos', d.attempt.content.id]"
        >← Volver a la evaluación</a
      >
      <section class="result-hero panel">
        <span class="eyebrow"
          >EVALUACIÓN FORMATIVA · INTENTO {{ d.attempt.attemptNumber }}</span
        >
        <div class="result-score">{{ d.attempt.percent }}<span>%</span></div>
        <h1>
          {{
            d.attempt.passed
              ? "¡Un paso más en tu aprendizaje!"
              : "Cada intento es una oportunidad"
          }}
        </h1>
        <p>{{ d.attempt.content.title }} · {{ d.attempt.student.fullName }}</p>
        <span
          class="badge"
          [class.positive]="d.attempt.passed"
          [class.warning]="!d.attempt.passed"
          >{{ d.attempt.passed ? "APROBADO" : "NO APROBADO" }}</span
        >
        <div class="stars">{{ "★".repeat(d.attempt.stars) }}</div>
        @if (d.attempt.passed && auth.user()?.role === "ESTUDIANTE") {
          <button
            class="button"
            [disabled]="saving()"
            (click)="complete(d.attempt.content.id)"
          >
            Registrar contenido completado ✓
          </button>
        }
      </section>
      <section class="panel section-space">
        <h2>Revisa tus respuestas</h2>
        @for (answer of d.answers; track answer.id; let index = $index) {
          <article class="answer-review">
            <div class="section-heading compact">
              <h3>{{ index + 1 }}. {{ answer.question.prompt }}</h3>
              <span
                class="badge"
                [class.positive]="answer.correct"
                [class.warning]="!answer.correct"
                >{{ answer.earnedPoints }}/{{
                  answer.question.points
                }}
                puntos</span
              >
            </div>
            <p><strong>Tu respuesta:</strong> {{ answer.answerText }}</p>
            <p>
              <strong>Respuesta esperada:</strong>
              {{ answer.question.correctAnswer }}
            </p>
            <p class="muted">{{ answer.question.explanation }}</p>
          </article>
        }
      </section>
    }
    @if (lesson(); as d) {
      <a
        class="back-link"
        [routerLink]="
          d.canLearn ? ['/aula/cursos', d.course.id] : ['/cursos', d.course.id]
        "
        >← {{ d.course.title }}</a
      >
      <div class="lesson-layout">
        <div class="lesson-main">
          <div class="page-heading">
            <span class="eyebrow"
              >{{ d.content.type }} · {{ d.content.moduleTitle }}</span
            >
            <h1>{{ d.content.title }}</h1>
          </div>
          <section class="panel">
            @if (d.content.type === "VIDEO") {
              @if (embed(); as url) {
                <iframe
                  class="video-frame"
                  [src]="url"
                  title="Recurso audiovisual del curso"
                  allowfullscreen
                ></iframe>
              } @else {
                <div class="video-placeholder">
                  <span>▷</span><strong>Recurso audiovisual</strong
                  ><small>{{
                    d.content.resourceUrl
                      ? "El video está disponible en el enlace del recurso."
                      : "El docente aún no ha vinculado un video."
                  }}</small>
                </div>
              }
            }
            <div class="lesson-body preserve-lines">{{ d.content.body }}</div>
            @if (d.content.resourceUrl) {
              <a
                class="button button-ghost"
                [href]="d.content.resourceUrl"
                target="_blank"
                rel="noopener noreferrer"
                >Abrir recurso externo ↗</a
              >
            }
            @if (d.content.hasFile && d.canLearn) {
              <a
                class="button button-ghost"
                [href]="'/api/contenidos/' + d.content.id + '/archivo'"
                >Descargar {{ d.content.fileName }} ↓</a
              >
            }
            @if (!d.canLearn) {
              <div class="soft-note">
                Estás viendo un contenido de acceso previo. Una matrícula
                vigente habilita la participación y el registro de tu progreso.
              </div>
            }
            @if (auth.user()?.role === "ESTUDIANTE" && d.canLearn) {
              <div class="completion-action">
                @if (d.completed.includes(d.content.id)) {
                  <span class="badge positive">✓ Contenido completado</span>
                } @else {
                  <button
                    class="button"
                    [disabled]="saving()"
                    (click)="complete(d.content.id)"
                  >
                    Marcar como completado ✓
                  </button>
                }
              </div>
            }
            @if (d.canManage) {
              <div class="actions section-space">
                <a
                  class="button button-ghost"
                  [routerLink]="['/gestion/contenidos', d.content.id, 'editar']"
                  >Editar contenido</a
                >
                @if (d.content.type === "EVALUACION") {
                  <a
                    class="button"
                    [routerLink]="[
                      '/gestion/contenidos',
                      d.content.id,
                      'preguntas',
                    ]"
                    >Gestionar preguntas</a
                  >
                }
              </div>
            }
          </section>
          @if (d.content.type === "ACTIVIDAD" && d.canLearn) {
            <section class="panel section-space">
              <span class="eyebrow">APLICA LO APRENDIDO</span>
              <h2>Entrega de actividad</h2>
              <p class="muted">
                Hasta {{ d.content.maxAttempts }} entregas. Nota de 0 a 20.
                Tamaño máximo: 10 MB.
              </p>
              @if (
                auth.user()?.role === "ESTUDIANTE" &&
                (d.submissions?.length || 0) < d.content.maxAttempts
              ) {
                <button type="button" class="button" (click)="submission()">
                  Enviar actividad
                </button>
              }
              <div
                class="table-wrap section-space"
                tabindex="0"
                role="region"
                aria-label="Entregas"
              >
                <table>
                  <thead>
                    <tr>
                      <th>Participante</th>
                      <th>Intento</th>
                      <th>Archivo</th>
                      <th>Nota</th>
                      <th>Retroalimentación</th>
                    </tr>
                  </thead>
                  <tbody>
                    @for (s of d.submissions || []; track s.id) {
                      <tr>
                        <td>{{ s.student.fullName }}</td>
                        <td>{{ s.attemptNumber }}</td>
                        <td>
                          <a
                            class="text-link"
                            [href]="'/api/entregas/' + s.id + '/archivo'"
                            >{{ s.fileName }} ↓</a
                          ><small>{{ s.comment }}</small>
                        </td>
                        <td>
                          {{ s.grade === null ? "Por calificar" : s.grade }}
                        </td>
                        <td>{{ s.feedback }}</td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
              @if (!d.submissions?.length) {
                <p class="muted">Aún no hay entregas registradas.</p>
              }
              @if (d.canManage) {
                <a
                  class="text-link"
                  [routerLink]="['/gestion/cursos', d.course.id, 'seguimiento']"
                  >Calificar desde seguimiento académico →</a
                >
              }
            </section>
          }
          @if (d.content.type === "EVALUACION" && d.canLearn) {
            <section class="panel section-space">
              <span class="eyebrow">APRENDE CON CADA INTENTO</span>
              <h2>Evaluación formativa</h2>
              <p class="muted">
                Aprobación: {{ d.content.passPercent }}% ·
                {{
                  d.content.maxAttempts === 0
                    ? "Intentos sin límite."
                    : "Máximo " + d.content.maxAttempts + " intentos."
                }}
              </p>
              @if (canEvaluate(d)) {
                <form
                  [formGroup]="answers"
                  (ngSubmit)="evaluate()"
                  class="exam-form"
                >
                  @for (
                    q of d.questions || [];
                    track q.id;
                    let index = $index
                  ) {
                    <fieldset class="question">
                      <legend>
                        <span class="question-number">{{ index + 1 }}</span
                        >{{ q.prompt }}<small>{{ q.points }} puntos</small>
                      </legend>
                      @switch (q.type) {
                        @case ("OPCION_MULTIPLE") {
                          @for (option of q.options; track option) {
                            <label class="radio-option"
                              ><input
                                type="radio"
                                [formControlName]="key(q, 0)"
                                [value]="option"
                                required
                              /><span>{{ option }}</span></label
                            >
                          }
                        }
                        @case ("VERDADERO_FALSO") {
                          @for (
                            option of ["Verdadero", "Falso"];
                            track option
                          ) {
                            <label class="radio-option"
                              ><input
                                type="radio"
                                [formControlName]="key(q, 0)"
                                [value]="option"
                                required
                              />{{ option }}</label
                            >
                          }
                        }
                        @case ("RELACIONAR") {
                          @for (
                            label of q.labels;
                            track $index;
                            let gap = $index
                          ) {
                            <label class="matching-row"
                              ><strong>{{ label }}</strong
                              ><select [formControlName]="key(q, gap)" required>
                                <option value="">Selecciona su relación</option>
                                @for (choice of q.choices; track choice) {
                                  <option [value]="choice">{{ choice }}</option>
                                }
                              </select></label
                            >
                          }
                        }
                        @case ("SELECCIONAR_ESPACIOS") {
                          @for (gap of gaps(q); track gap) {
                            <label
                              >Espacio {{ gap + 1
                              }}<select
                                [formControlName]="key(q, gap)"
                                required
                              >
                                <option value="">Selecciona un término</option>
                                @for (choice of q.options; track choice) {
                                  <option [value]="choice">{{ choice }}</option>
                                }
                              </select></label
                            >
                          }
                        }
                        @default {
                          @for (gap of gaps(q); track gap) {
                            <label
                              >Respuesta {{ gap + 1
                              }}<input
                                [formControlName]="key(q, gap)"
                                maxlength="2000"
                                required
                                autocomplete="off"
                            /></label>
                          }
                        }
                      }
                    </fieldset>
                  }
                  <button class="button" [disabled]="saving()">
                    Enviar evaluación →
                  </button>
                </form>
              }
              @if (!d.content.examActive) {
                <p class="muted">La evaluación está desactivada.</p>
              }
              @if (
                auth.user()?.role === "ESTUDIANTE" &&
                d.content.maxAttempts > 0 &&
                (d.attempts?.length || 0) >= d.content.maxAttempts
              ) {
                <p class="muted">Alcanzaste el máximo de intentos.</p>
              }
              @if (!d.questions?.length) {
                <p class="muted">
                  La evaluación aún no tiene preguntas configuradas.
                </p>
              }
              @if (d.attempts?.length) {
                <h3>Intentos registrados</h3>
                <div
                  class="table-wrap"
                  tabindex="0"
                  role="region"
                  aria-label="Intentos"
                >
                  <table>
                    <thead>
                      <tr>
                        <th>Participante</th>
                        <th>Intento</th>
                        <th>Resultado</th>
                        <th>Estrellas</th>
                        <th>Detalle</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (a of d.attempts || []; track a.id) {
                        <tr>
                          <td>{{ a.student.fullName }}</td>
                          <td>{{ a.attemptNumber }}</td>
                          <td>
                            <span
                              class="badge"
                              [class.positive]="a.passed"
                              [class.warning]="!a.passed"
                              >{{ a.percent }}%</span
                            >
                          </td>
                          <td>{{ a.stars }} ★</td>
                          <td>
                            <a
                              class="text-link"
                              [routerLink]="['/evaluaciones/intentos', a.id]"
                              >Ver resultado →</a
                            >
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </section>
          }
          @if (d.canLearn) {
            <section class="panel section-space" id="participacion">
              <span class="eyebrow">CONSTRUIMOS CONOCIMIENTO JUNTOS</span>
              <h2>
                {{
                  d.content.type === "FORO"
                    ? "Foro académico"
                    : "Conversación del contenido"
                }}
              </h2>
              <button type="button" class="button" (click)="post()">
                + Publicar aporte
              </button>
              @for (row of d.posts; track row.post.id) {
                @let p = row.post;
                <article class="discussion" [class.reply]="p.parentId !== null">
                  <div class="discussion-author">
                    <span class="avatar">{{ p.author.initials }}</span>
                    <div>
                      <strong>{{ p.author.fullName }}</strong
                      ><small
                        >{{ p.author.role }} · {{ date(p.createdAt) }}</small
                      >
                    </div>
                    @if (p.parentId) {
                      <span class="badge"
                        >Respuesta a {{ p.parentAuthor }}</span
                      >
                    }
                  </div>
                  <p class="preserve-lines">{{ p.body }}</p>
                  @if (!p.deleted) {
                    <div class="discussion-actions">
                      <div class="actions">
                        <select
                          [ngModel]="reactions[p.id] || 'UTIL'"
                          (ngModelChange)="reactions[p.id] = $event"
                          aria-label="Reacción"
                        >
                          <option value="UTIL">Útil</option>
                          <option value="ME_GUSTA">Me gusta</option>
                          <option value="INTERESANTE">
                            Interesante
                          </option></select
                        ><button
                          class="link-button"
                          [disabled]="saving()"
                          (click)="react(p.id)"
                        >
                          Reaccionar · {{ row.reactionCount }}
                        </button>
                      </div>
                      @if (p.author.id !== auth.user()?.id) {
                        <div class="actions">
                          <select
                            [ngModel]="ratings[p.id] || 1"
                            (ngModelChange)="ratings[p.id] = $event"
                            aria-label="Puntuación del aporte"
                          >
                            @for (n of [1, 2, 3, 4, 5]; track n) {
                              <option [ngValue]="n">{{ n }} ★</option>
                            }</select
                          ><button
                            class="link-button"
                            [disabled]="saving()"
                            (click)="rate(p.id)"
                          >
                            Valorar · {{ row.rating }}
                          </button>
                        </div>
                      }
                    </div>
                    <div class="actions">
                      <button
                        type="button"
                        class="button button-ghost button-small"
                        (click)="post(p)"
                      >
                        Responder
                      </button>
                      @if (p.author.id === auth.user()?.id) {
                        <button
                          type="button"
                          class="button button-ghost button-small"
                          (click)="post(p, true)"
                        >
                          Editar mi aporte
                        </button>
                      }
                      @if (p.author.id === auth.user()?.id || d.canManage) {
                        <button
                          type="button"
                          class="link-button danger"
                          [disabled]="saving()"
                          (click)="remove(p.id)"
                        >
                          Retirar publicación
                        </button>
                      }
                    </div>
                  }
                </article>
              }
              @if (!d.posts.length) {
                <p class="muted">
                  Sé la primera persona en compartir una idea.
                </p>
              }
            </section>
          }
        </div>
        <aside class="lesson-sidebar panel">
          <span class="eyebrow">CONTENIDO DEL CURSO</span>
          <h3>Tu ruta</h3>
          @for (row of d.syllabus; track row.module.id) {
            <h4>{{ row.module.title }}</h4>
            @for (c of row.contents; track c.id) {
              @if (d.canLearn || c.preview) {
                <a
                  class="lesson-nav"
                  [class.selected]="c.id === d.content.id"
                  [routerLink]="[
                    d.canLearn ? '/contenidos' : '/vista-previa',
                    c.id,
                  ]"
                  ><span>{{
                    d.completed.includes(c.id) ? "✓" : c.position
                  }}</span
                  >{{ c.title }}</a
                >
              }
            }
          }
        </aside>
      </div>
    }
    <inudi-editor />
  `,
})
export class LearningComponent extends AsyncPage {
  @ViewChild(EditorComponent) editor!: EditorComponent;
  lesson = signal<Lesson | null>(null);
  result = signal<{ attempt: Attempt; answers: Answer[] } | null>(null);
  embed = signal<SafeResourceUrl | null>(null);
  sanitizer = inject(DomSanitizer);
  answers = new FormGroup<Record<string, FormControl<string>>>({});
  reactions: Record<number, string> = {};
  ratings: Record<number, number> = {};
  constructor() {
    super();
    this.watch(() => this.refresh());
  }
  key(q: Question, index: number): string {
    return "q_" + q.id + "_" + index;
  }
  gaps(q: Question): number[] {
    return Array.from(
      {
        length:
          q.type === "RELACIONAR"
            ? q.labels.length
            : q.type === "OPCION_MULTIPLE" || q.type === "VERDADERO_FALSO"
              ? 1
              : q.gapCount,
      },
      (_, index) => index,
    );
  }
  canEvaluate(d: Lesson): boolean {
    return (
      this.auth.user()?.role === "ESTUDIANTE" &&
      d.content.examActive &&
      (d.content.maxAttempts === 0 ||
        (d.attempts?.length || 0) < d.content.maxAttempts) &&
      !!d.questions?.length
    );
  }
  async refresh(): Promise<void> {
    await this.load(async () => {
      const id = this.route.snapshot.paramMap.get("id");
      if (this.route.snapshot.data["view"] === "result") {
        this.lesson.set(null);
        this.result.set(await this.api.get("/evaluaciones/intentos/" + id));
        return;
      }
      const d = await this.api.get<Lesson>("/contenidos/" + id);
      this.lesson.set(d);
      this.result.set(null);
      const match = d.content.resourceUrl?.match(
        /^https:\/\/(?:www\.)?(?:youtube\.com\/watch\?v=|youtu\.be\/)([A-Za-z0-9_-]{11})(?:[?&].*)?$/,
      );
      this.embed.set(
        match
          ? this.sanitizer.bypassSecurityTrustResourceUrl(
              "https://www.youtube-nocookie.com/embed/" + match[1],
            )
          : null,
      );
      const controls: Record<string, FormControl<string>> = {};
      for (const q of d.questions || [])
        for (const gap of this.gaps(q))
          controls[this.key(q, gap)] = new FormControl("", {
            nonNullable: true,
            validators: [Validators.required, Validators.maxLength(2000)],
          });
      this.answers = new FormGroup(controls);
    });
  }
  async complete(id: number): Promise<void> {
    await this.action(
      () => this.api.send("POST", "/contenidos/" + id + "/completar"),
      () => this.refresh(),
      "Progreso guardado.",
    );
  }
  submission(): void {
    const id = this.lesson()!.content.id;
    this.editor.open({
      title: "Enviar actividad",
      button: "Enviar actividad ↑",
      fields: [
        {
          name: "file",
          label: "Archivo de tu actividad",
          type: "file",
          required: true,
          help: "PDF, documentos de Office, TXT, imágenes o ZIP. Tamaño máximo: 10 MB.",
        },
        {
          name: "comment",
          label: "Comentario para el docente",
          type: "textarea",
          maxLength: 1000,
        },
      ],
      save: async (value) => {
        await this.api.upload(
          "/contenidos/" + id + "/entregar",
          value["file"] as File,
          value["comment"] as string,
        );
        this.notice.set(
          "Actividad enviada. Puedes consultar aquí la nota y la retroalimentación.",
        );
        await this.refresh();
      },
    });
  }
  async evaluate(): Promise<void> {
    this.answers.markAllAsTouched();
    if (this.answers.invalid) {
      this.error.set(
        "Responde todas las preguntas antes de enviar la evaluación.",
      );
      return;
    }
    const d = this.lesson()!;
    const replies: Record<string, string[]> = {};
    for (const q of d.questions || [])
      replies["q_" + q.id] = this.gaps(q).map(
        (gap) => this.answers.controls[this.key(q, gap)].value,
      );
    await this.action(
      async () => {
        const attempt = await this.api.send<Attempt>(
          "POST",
          "/contenidos/" + d.content.id + "/evaluar",
          replies,
        );
        await this.router.navigate(["/evaluaciones/intentos", attempt.id]);
      },
      async () => {},
      "Evaluación registrada.",
    );
  }
  post(p?: Post, edit = false): void {
    const d = this.lesson()!;
    this.editor.open({
      title: edit
        ? "Editar mi aporte"
        : p
          ? "Responder aporte"
          : "Publicar aporte",
      button: edit
        ? "Guardar edición"
        : p
          ? "Publicar respuesta"
          : "Publicar aporte →",
      fields: [
        {
          name: "body",
          label: edit
            ? "Contenido"
            : p
              ? "Tu respuesta"
              : "Comparte una idea o pregunta",
          type: "textarea",
          required: true,
          maxLength: 4000,
        },
      ],
      initial: { body: edit ? p?.body : "" },
      save: async (value) => {
        await this.api.send(
          edit ? "PUT" : "POST",
          edit
            ? "/publicaciones/" + p!.id
            : "/contenidos/" + d.content.id + "/publicaciones",
          { ...value, parentId: edit ? null : p?.id || null },
        );
        await this.refresh();
      },
    });
  }
  async remove(id: number): Promise<void> {
    if (
      !window.confirm(
        "¿Retirar esta publicación? Las respuestas se conservarán.",
      )
    )
      return;
    await this.action(
      () => this.api.send("DELETE", "/publicaciones/" + id),
      () => this.refresh(),
      "Publicación retirada.",
    );
  }
  async react(id: number): Promise<void> {
    await this.action(
      () =>
        this.api.send("POST", "/publicaciones/" + id + "/reaccion", {
          type: this.reactions[id] || "UTIL",
        }),
      () => this.refresh(),
      "Reacción registrada.",
    );
  }
  async rate(id: number): Promise<void> {
    await this.action(
      () =>
        this.api.send("POST", "/publicaciones/" + id + "/valorar", {
          score: this.ratings[id] || 1,
        }),
      () => this.refresh(),
      "Valoración registrada.",
    );
  }
}
