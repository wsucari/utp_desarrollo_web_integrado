import { Component, signal, ViewChild } from "@angular/core";
import { DecimalPipe } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { AsyncPage } from "../shared/async-page";
import { StatusComponent } from "../shared/status";
import { CommentsComponent } from "../shared/comments";
import { EditorComponent } from "../shared/editor";
import { accountFields, paymentFields } from "../shared/forms";
import {
  Account,
  Audit,
  Certificate,
  Course,
  Enrollment,
  Options,
  Payment,
  Post,
  UserSession,
} from "../core/models";

@Component({
  standalone: true,
  imports: [
    FormsModule,
    DecimalPipe,
    RouterLink,
    StatusComponent,
    CommentsComponent,
    EditorComponent,
  ],
  template: `
    <inudi-status
      [loading]="loading()"
      [error]="error()"
      [notice]="notice()"
      (retry)="refresh()"
    />
    @switch (view) {
      @case ("users") {
        @let teachers = teacherView;
        <div class="page-heading with-action">
          <h1>{{ teachers ? "Profesores" : "Usuarios" }}</h1>
          <button type="button" class="button" (click)="editUser()">
            + {{ teachers ? "Nuevo profesor" : "Nuevo usuario" }}
          </button>
        </div>
        <section class="panel">
          <label class="search-field table-search"
            ><svg><use href="#i-search" /></svg
            ><input
              [(ngModel)]="query"
              placeholder="Buscar por nombre, correo o rol..."
              aria-label="Buscar usuario"
          /></label>
          <div
            class="table-wrap"
            tabindex="0"
            role="region"
            aria-label="Usuarios"
          >
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Correo</th>
                  <th>Rol</th>
                  <th>Especialidad</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                @for (a of accounts(); track a.id) {
                  @if (includes(a, query)) {
                    <tr>
                      <td>
                        <div class="person-cell">
                          <span class="avatar">{{ a.initials }}</span
                          ><strong>{{ a.fullName }}</strong>
                        </div>
                      </td>
                      <td>{{ a.email }}</td>
                      <td>
                        <span class="badge">{{ a.role }}</span>
                      </td>
                      <td>{{ a.professionalTitle }}</td>
                      <td>
                        <span
                          class="badge"
                          [class.positive]="a.active"
                          [class.warning]="!a.active"
                          >{{ a.active ? "ACTIVO" : "INACTIVO" }}</span
                        >
                      </td>
                      <td>
                        <button
                          type="button"
                          class="link-button text-link"
                          (click)="editUser(a)"
                        >
                          Editar →
                        </button>
                      </td>
                    </tr>
                  }
                }
              </tbody>
            </table>
          </div>
          @if (!accounts().length) {
            <p class="muted">No hay usuarios registrados.</p>
          }
        </section>
      }
      @case ("payments") {
        <div class="page-heading with-action">
          <div>
            <h1>Pagos</h1>
            <p>Registro administrativo de pagos y vigencia de matrículas.</p>
          </div>
          <button type="button" class="button" (click)="editPayment()">
            + Registrar pago
          </button>
        </div>
        <section class="panel">
          <label class="search-field table-search"
            ><svg><use href="#i-search" /></svg
            ><input
              [(ngModel)]="query"
              placeholder="Buscar estudiante, curso o referencia"
              aria-label="Buscar pagos"
          /></label>
          <div class="table-wrap" tabindex="0" role="region" aria-label="Pagos">
            <table>
              <thead>
                <tr>
                  <th>Estudiante / curso</th>
                  <th>Importe</th>
                  <th>Referencia</th>
                  <th>Estado</th>
                  <th>Acceso hasta</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                @for (p of payments(); track p.id) {
                  @if (includes(p, query)) {
                    <tr>
                      <td>
                        <strong>{{ p.student.fullName }}</strong
                        ><small>{{ p.course.title }}</small>
                      </td>
                      <td>S/ {{ p.amount | number: "1.2-2" }}</td>
                      <td>
                        {{ p.reference }}<small>{{ date(p.createdAt) }}</small>
                      </td>
                      <td>
                        <span
                          class="badge"
                          [class.positive]="p.status === 'CONFIRMADO'"
                          [class.warning]="p.status === 'PENDIENTE'"
                          >{{ p.status }}</span
                        >
                      </td>
                      <td>{{ paymentDates()[p.id] || "—" }}</td>
                      <td>
                        <button
                          type="button"
                          class="link-button text-link"
                          (click)="editPayment(p)"
                        >
                          Editar
                        </button>
                      </td>
                    </tr>
                  }
                }
              </tbody>
            </table>
          </div>
          @if (!payments().length) {
            <p class="muted">No hay pagos registrados.</p>
          }
        </section>
      }
      @case ("comments") {
        <h1 class="tw-text-2xl tw-font-semibold">Reseñas</h1>
        <inudi-comments [comments]="comments()" />
      }
      @case ("sessions") {
        <h1 class="tw-text-2xl tw-font-semibold">Sesiones</h1>
        <form
          (ngSubmit)="filter()"
          class="tw-flex tw-flex-wrap tw-gap-3 tw-my-6"
        >
          <input
            name="search"
            [(ngModel)]="query"
            placeholder="Buscar por nombre o correo"
            aria-label="Buscar sesiones"
          /><select
            name="sesion"
            [(ngModel)]="state"
            aria-label="Estado de sesión"
          >
            <option value="">Todas las sesiones</option>
            <option value="en sesion">En sesión</option>
            <option value="sesion terminada">Sesión terminada</option></select
          ><button class="button">Buscar</button>
        </form>
        <section class="tw-bg-white tw-rounded-lg tw-shadow tw-p-5">
          <div
            class="table-wrap"
            tabindex="0"
            role="region"
            aria-label="Sesiones"
          >
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Correo</th>
                  <th>Inicio de sesión</th>
                  <th>Fin de sesión</th>
                  <th>Duración</th>
                  <th>Rol</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                @for (record of sessions(); track record.id) {
                  <tr>
                    <td>{{ record.account.fullName }}</td>
                    <td>{{ record.account.email }}</td>
                    <td>{{ record.loggedInAt }}</td>
                    <td>{{ record.loggedOutAt }}</td>
                    <td>{{ record.duration }}</td>
                    <td>{{ record.roleAtLogin }}</td>
                    <td>
                      {{ record.current ? "En sesión" : "Sesión terminada" }}
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          @if (!sessions().length) {
            <p>No hay sesiones registradas.</p>
          }
        </section>
      }
      @case ("preview") {
        <h1 class="tw-text-2xl tw-font-semibold">Ver como</h1>
        <form
          (ngSubmit)="filter()"
          class="tw-flex tw-flex-wrap tw-gap-3 tw-my-6"
        >
          <input
            name="search"
            [(ngModel)]="query"
            placeholder="Buscar por nombre o correo"
            aria-label="Buscar usuario"
          /><select name="roleFilter" [(ngModel)]="state" aria-label="Perfil">
            <option value="">Todos</option>
            <option value="ESTUDIANTE">Estudiantes</option>
            <option value="DOCENTE">Profesores</option></select
          ><button class="button">Buscar</button>
        </form>
        <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-4">
          @for (a of accounts(); track a.id) {
            <article
              class="tw-bg-white tw-border tw-border-gray-200 tw-rounded-lg tw-p-5"
            >
              <span class="avatar">{{ a.initials }}</span>
              <h2 class="tw-text-lg tw-mt-3">{{ a.fullName }}</h2>
              <p class="tw-text-gray-500">{{ a.email }}</p>
              <p>{{ a.role === "DOCENTE" ? "Profesor" : "Estudiante" }}</p>
              <a
                class="button button-small"
                [routerLink]="[
                  '/admin/vista-previa',
                  a.role === 'DOCENTE' ? 'profesor' : 'estudiante',
                  a.id,
                ]"
                >Ver como</a
              >
            </article>
          }
        </div>
      }
      @case ("previewAccount") {
        @if (selected(); as a) {
          <div
            class="tw-bg-amber-50 tw-border tw-border-amber-200 tw-rounded-lg tw-p-4 tw-mb-6"
          >
            Vista previa de solo lectura. La sesión de administración se
            mantiene.
          </div>
          <h1 class="tw-text-2xl tw-font-semibold">{{ a.fullName }}</h1>
          <p class="muted">{{ a.email }}</p>
          <h2 class="tw-text-xl tw-mt-6">
            {{ a.role === "DOCENTE" ? "Cursos asignados" : "Cursos inscritos" }}
          </h2>
          <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-5">
            @for (c of previewCourses(); track c.id) {
              <article class="panel">
                <h3>{{ c.title }}</h3>
                <p>{{ c.category }}</p>
              </article>
            }
          </div>
          <a routerLink="/admin/vista-previa" class="text-link section-space"
            >Volver</a
          >
        }
      }
      @case ("enrollments") {
        <div class="page-heading"><h1>Matrículas y vigencia</h1></div>
        <section class="panel">
          <div
            class="table-wrap"
            tabindex="0"
            role="region"
            aria-label="Matrículas"
          >
            <table>
              <thead>
                <tr>
                  <th>Estudiante</th>
                  <th>Curso</th>
                  <th>Inicio</th>
                  <th>Vencimiento</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                @for (e of enrollments(); track e.id) {
                  <tr>
                    <td>{{ e.student.fullName }}</td>
                    <td>{{ e.course.title }}</td>
                    <td>{{ date(e.startsAt) }}</td>
                    <td>{{ date(e.expiresAt) }}</td>
                    <td>
                      <span class="badge" [class.positive]="e.current">{{
                        e.current ? "VIGENTE" : e.status
                      }}</span>
                    </td>
                    <td>
                      @if (e.status === "ACTIVA") {
                        <button
                          class="link-button danger"
                          [disabled]="saving()"
                          (click)="revoke(e.id)"
                        >
                          Cancelar acceso
                        </button>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          @if (!enrollments().length) {
            <p>No hay matrículas registradas.</p>
          }
        </section>
      }
      @case ("certificates") {
        <div class="page-heading"><h1>Certificados emitidos</h1></div>
        <section class="panel">
          <div
            class="table-wrap"
            tabindex="0"
            role="region"
            aria-label="Certificados"
          >
            <table>
              <thead>
                <tr>
                  <th>Participante</th>
                  <th>Curso</th>
                  <th>Código</th>
                  <th>Fecha</th>
                  <th>Documento</th>
                </tr>
              </thead>
              <tbody>
                @for (c of certificates(); track c.id) {
                  <tr>
                    <td>{{ c.studentName }}</td>
                    <td>{{ c.courseTitle }}</td>
                    <td>{{ c.code }}</td>
                    <td>{{ date(c.issuedAt) }}</td>
                    <td>
                      <a
                        class="text-link"
                        [href]="'/api/certificados/' + c.id + '/pdf'"
                        >Descargar PDF ↓</a
                      >
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          @if (!certificates().length) {
            <p>No hay certificados emitidos.</p>
          }
        </section>
      }
      @case ("audit") {
        <div class="page-heading"><h1>Registro de operaciones</h1></div>
        <section class="panel">
          <div
            class="table-wrap"
            tabindex="0"
            role="region"
            aria-label="Operaciones"
          >
            <table>
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Usuario</th>
                  <th>Acción</th>
                  <th>Detalle</th>
                </tr>
              </thead>
              <tbody>
                @for (e of entries(); track e.id) {
                  <tr>
                    <td>{{ e.createdAt }}</td>
                    <td>{{ e.actor.fullName }}</td>
                    <td>{{ e.action }}</td>
                    <td>{{ e.details }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          @if (!entries().length) {
            <p>No hay operaciones registradas.</p>
          }
        </section>
      }
    }
    <inudi-editor />
  `,
})
export class AdminComponent extends AsyncPage {
  @ViewChild(EditorComponent) editor!: EditorComponent;
  view = this.route.snapshot.data["view"] as string;
  teacherView = !!this.route.snapshot.data["teachers"];
  query = "";
  state = "";
  accounts = signal<Account[]>([]);
  payments = signal<Payment[]>([]);
  paymentDates = signal<Record<string, string>>({});
  comments = signal<Post[]>([]);
  sessions = signal<UserSession[]>([]);
  selected = signal<Account | null>(null);
  previewCourses = signal<Course[]>([]);
  enrollments = signal<Enrollment[]>([]);
  certificates = signal<Certificate[]>([]);
  entries = signal<Audit[]>([]);
  constructor() {
    super();
    this.watch(() => this.refresh());
  }
  async refresh(): Promise<void> {
    await this.load(async () => {
      switch (this.view) {
        case "users": {
          const d = await this.api.get<{ accounts: Account[] }>(
            "/admin/usuarios",
            this.teacherView ? { role: "DOCENTE" } : {},
          );
          this.accounts.set(d.accounts);
          break;
        }
        case "payments": {
          const d = await this.api.get<{
            payments: Payment[];
            paymentDates: Record<string, string>;
          }>("/admin/pagos");
          this.payments.set(d.payments);
          this.paymentDates.set(d.paymentDates);
          break;
        }
        case "comments":
          this.comments.set(await this.api.get<Post[]>("/admin/resenas"));
          break;
        case "sessions":
          this.query = this.route.snapshot.queryParamMap.get("search") || "";
          this.state = this.route.snapshot.queryParamMap.get("sesion") || "";
          this.sessions.set(
            await this.api.get<UserSession[]>("/admin/sesiones", {
              search: this.query,
              sesion: this.state,
            }),
          );
          break;
        case "preview":
          this.query = this.route.snapshot.queryParamMap.get("search") || "";
          this.state =
            this.route.snapshot.queryParamMap.get("roleFilter") || "";
          this.accounts.set(
            await this.api.get<Account[]>("/admin/vista-previa", {
              search: this.query,
              roleFilter: this.state,
            }),
          );
          break;
        case "previewAccount": {
          const d = await this.api.get<{
            selected: Account;
            previewCourses: Course[];
          }>(
            "/admin/vista-previa/" +
              this.route.snapshot.paramMap.get("profile") +
              "/" +
              this.route.snapshot.paramMap.get("id"),
          );
          this.selected.set(d.selected);
          this.previewCourses.set(d.previewCourses);
          break;
        }
        case "enrollments":
          this.enrollments.set(
            await this.api.get<Enrollment[]>("/admin/matriculas"),
          );
          break;
        case "certificates":
          this.certificates.set(
            await this.api.get<Certificate[]>("/admin/certificados"),
          );
          break;
        case "audit":
          this.entries.set(await this.api.get<Audit[]>("/admin/auditoria"));
          break;
      }
      if (this.route.snapshot.data["editor"] === "user") {
        const id = this.route.snapshot.paramMap.get("id");
        const a = id
          ? this.accounts().find((a) => a.id === Number(id))
          : undefined;
        if (id && !a) throw new Error("El usuario no existe.");
        this.editUser(a);
      }
      if (this.route.snapshot.data["editor"] === "payment") {
        const id = this.route.snapshot.paramMap.get("id");
        const p = id
          ? this.payments().find((p) => p.id === Number(id))
          : undefined;
        if (id && !p) throw new Error("El pago no existe.");
        await this.editPayment(p);
      }
    });
  }
  filter(): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        search: this.query,
        [this.view === "sessions" ? "sesion" : "roleFilter"]: this.state,
      },
    });
  }
  editUser(a?: Account): void {
    this.editor.open({
      title: a
        ? "Editar usuario"
        : this.teacherView
          ? "Nuevo profesor"
          : "Nuevo usuario",
      fields: accountFields(!!a),
      initial: a
        ? { ...a, password: "" }
        : {
            fullName: "",
            email: "",
            role:
              this.teacherView ||
              this.route.snapshot.queryParamMap.get("initialRole") === "DOCENTE"
                ? "DOCENTE"
                : "ESTUDIANTE",
            active: true,
          },
      save: async (value) => {
        await this.api.send(
          a ? "PUT" : "POST",
          "/admin/usuarios" + (a ? "/" + a.id : ""),
          value,
        );
        this.notice.set("Usuario guardado.");
        await this.afterEditor("/admin/usuarios");
      },
    });
  }
  async editPayment(p?: Payment): Promise<void> {
    try {
      const options = await this.api.get<Options>("/admin/opciones");
      this.editor.open({
        title: p ? "Editar pago" : "Registrar pago",
        fields: paymentFields(options, !!p),
        help: "El registro confirmado habilita la matrícula hasta la fecha indicada. No se procesa ningún cobro externo. El estudiante y el curso se conservan al editar.",
        button: p ? "Guardar cambios" : "Registrar pago",
        initial: p
          ? {
              studentId: p.student.id,
              courseId: p.course.id,
              amount: p.amount,
              reference: p.reference,
              status: p.status,
              expiration: this.paymentDates()[p.id] || options.expiration,
            }
          : {
              studentId: options.students[0]?.id,
              courseId: options.courses[0]?.id,
              amount: 0,
              status: "CONFIRMADO",
              expiration: options.expiration,
            },
        save: async (value) => {
          await this.api.send(
            p ? "PUT" : "POST",
            "/admin/pagos" + (p ? "/" + p.id : ""),
            { ...value, ...(p ? { version: p.version } : {}) },
          );
          this.notice.set(p ? "Pago actualizado." : "Pago registrado.");
          await this.afterEditor("/admin/pagos");
        },
      });
    } catch (e) {
      await this.load(async () => {
        throw e;
      });
    }
  }
  async afterEditor(path: string): Promise<void> {
    if (this.route.snapshot.data["editor"]) await this.router.navigate([path]);
    else await this.refresh();
  }
  async revoke(id: number): Promise<void> {
    if (
      !window.confirm("¿Cancelar el acceso conservando el historial académico?")
    )
      return;
    await this.action(
      () => this.api.send("POST", "/admin/matriculas/" + id + "/cancelar"),
      () => this.refresh(),
      "Acceso cancelado. El historial académico se conserva.",
    );
  }
}
