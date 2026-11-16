import { Component, inject, signal } from "@angular/core";
import { DecimalPipe } from "@angular/common";
import {
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { RouterLink } from "@angular/router";
import { AsyncPage } from "../shared/async-page";
import { StatusComponent } from "../shared/status";
import { EnrolledComponent } from "../shared/cards";
import { CommentsComponent } from "../shared/comments";
import {
  Account,
  Certificate,
  Course,
  CourseRow,
  Payment,
  Post,
} from "../core/models";
@Component({
  standalone: true,
  imports: [
    DecimalPipe,
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    StatusComponent,
    EnrolledComponent,
    CommentsComponent,
  ],
  template: `
    <inudi-status
      [loading]="loading()"
      [error]="error()"
      [notice]="notice()"
      (retry)="refresh()"
    />
    @switch (view) {
      @case ("profile") {
        <div class="page-heading"><h1>Mi perfil</h1></div>
        <div class="two-column">
          <section class="panel">
            <h2>Información personal</h2>
            <form
              [formGroup]="profile"
              (ngSubmit)="saveProfile()"
              class="stack-form"
            >
              <label
                >Nombre completo<input
                  formControlName="fullName"
                  maxlength="120"
                  required /></label
              ><label
                >Correo electrónico<input
                  type="email"
                  [value]="auth.user()?.email"
                  disabled /></label
              ><label
                >Teléfono<input formControlName="phone" maxlength="30" /></label
              ><label
                >Especialidad o título profesional<input
                  formControlName="professionalTitle"
                  maxlength="180"
              /></label>
              <h3>Cambiar contraseña</h3>
              <p class="muted small">
                Deja estos campos vacíos si solo deseas actualizar tus datos.
              </p>
              <label
                >Contraseña actual<input
                  type="password"
                  formControlName="oldPassword"
                  autocomplete="current-password" /></label
              ><label
                >Nueva contraseña<input
                  type="password"
                  formControlName="newPassword"
                  minlength="10"
                  maxlength="72"
                  autocomplete="new-password" /></label
              ><small
                >Al menos 10 caracteres con mayúsculas, minúsculas y
                números.</small
              ><button class="button" [disabled]="saving()">
                Guardar cambios
              </button>
            </form>
          </section>
          <aside class="panel profile-summary">
            <span class="avatar avatar-large">{{ auth.user()?.initials }}</span>
            <h2>{{ auth.user()?.fullName }}</h2>
            <span class="badge">{{ auth.user()?.role }}</span>
          </aside>
        </div>
      }
      @case ("courses") {
        <div class="page-heading"><h1>Cursos Inscritos</h1></div>
        <inudi-enrolled [rows]="myCourses()" />
      }
      @case ("cart") {
        <div class="page-heading">
          <h1>Carrito de compras</h1>
          <p>
            Confirma tu selección para registrar una solicitud de matrícula.
          </p>
        </div>
        <div class="two-column">
          <section class="panel">
            @for (item of cart(); track item.id) {
              <div class="cart-row">
                <div>
                  <h3>{{ item.course.title }}</h3>
                  <span class="muted"
                    >{{ item.course.category }} ·
                    {{ item.course.durationHours }} horas</span
                  >
                </div>
                <strong>S/ {{ item.course.price | number: "1.2-2" }}</strong
                ><button
                  type="button"
                  class="link-button danger"
                  [disabled]="saving()"
                  (click)="remove(item.course.id)"
                >
                  Retirar
                </button>
              </div>
            }
            @if (!cart().length) {
              <div class="empty-state">
                <svg><use href="#i-cart" /></svg>
                <h2>No hay cursos en tu selección</h2>
                <a class="button" routerLink="/">Explorar cursos</a>
              </div>
            }
          </section>
          <aside class="panel">
            <h2>Resumen</h2>
            <div class="total-row">
              <span>Total</span
              ><strong>S/ {{ total() | number: "1.2-2" }}</strong>
            </div>
            <p class="muted">
              Esta operación registra una solicitud. El acceso al curso se
              habilita después de la validación administrativa del pago.
            </p>
            @if (cart().length) {
              <button
                class="button button-full"
                [disabled]="saving()"
                (click)="checkout()"
              >
                Solicitar matrícula →
              </button>
            }
            <small
              >No se procesa ningún cobro bancario en esta aplicación.</small
            >
          </aside>
        </div>
      }
      @case ("payments") {
        <div class="page-heading">
          <span class="eyebrow">HISTORIAL DE INSCRIPCIÓN</span>
          <h1>Mis solicitudes y pagos</h1>
          <p>Consulta el estado de validación de tus matrículas.</p>
        </div>
        <section class="panel">
          <div
            class="table-wrap"
            tabindex="0"
            role="region"
            aria-label="Mis pagos"
          >
            <table>
              <thead>
                <tr>
                  <th>Curso</th>
                  <th>Importe</th>
                  <th>Referencia</th>
                  <th>Estado</th>
                  <th>Fecha</th>
                </tr>
              </thead>
              <tbody>
                @for (p of payments(); track p.id) {
                  <tr>
                    <td>{{ p.course.title }}</td>
                    <td>S/ {{ p.amount | number: "1.2-2" }}</td>
                    <td>{{ p.reference }}</td>
                    <td>
                      <span
                        class="badge"
                        [class.positive]="p.status === 'CONFIRMADO'"
                        [class.warning]="p.status === 'PENDIENTE'"
                        >{{ p.status }}</span
                      >
                    </td>
                    <td>{{ date(p.createdAt) }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          @if (!payments().length) {
            <p class="muted">Todavía no hay solicitudes registradas.</p>
          }
        </section>
      }
      @case ("certificates") {
        <div class="page-heading">
          <h1>Certificados</h1>
          <p>
            Completa tu ruta de aprendizaje y obtén un certificado verificable.
          </p>
        </div>
        <div class="learning-grid">
          @for (cert of certificates(); track cert.id) {
            <article class="panel certificate-card">
              <span class="stat-icon sand"
                ><svg><use href="#i-award" /></svg></span
              ><span class="eyebrow">CERTIFICADO EMITIDO</span>
              <h2>{{ cert.courseTitle }}</h2>
              <p>{{ cert.studentName }}</p>
              <span class="badge positive">Código {{ cert.code }}</span>
              <p class="muted small">Emitido el {{ date(cert.issuedAt) }}</p>
              <div class="actions">
                <a
                  class="button button-small"
                  [href]="'/api/certificados/' + cert.id + '/pdf'"
                  >Descargar PDF ↓</a
                ><a class="text-link" [routerLink]="['/cert', cert.code]"
                  >Validar ↗</a
                >
              </div>
            </article>
          }
        </div>
        @if (!certificates().length) {
          <div class="empty-state">
            <svg><use href="#i-award" /></svg>
            <h2>No hay certificados emitidos</h2>
            <p>
              Los certificados se habilitan al completar el 100 % de un curso
              con certificación y matrícula vigente.
            </p>
          </div>
        }
        <section class="panel section-space">
          <h2>Tu avance hacia el certificado</h2>
          @for (row of myCourses(); track row.enrollment.id) {
            @if (row.course.certificateEnabled) {
              <div class="certificate-progress">
                <div>
                  <h3>{{ row.course.title }}</h3>
                  <progress max="100" [value]="row.percent"></progress
                  ><small>{{ row.percent }}% completado</small>
                </div>
                @if (row.percent === 100 && row.enrollment.current) {
                  <button
                    class="button"
                    [disabled]="saving()"
                    (click)="issue(row.course.id)"
                  >
                    Obtener certificado
                  </button>
                }
              </div>
            }
          }
        </section>
      }
      @case ("comments") {
        <h1 class="tw-text-2xl tw-font-semibold">Mis comentarios</h1>
        <inudi-comments [comments]="comments()" />
      }
      @case ("verify") {
        <div class="narrow">
          <div class="page-heading">
            <span class="eyebrow">CONFIRMA UN LOGRO ACADÉMICO</span>
            <h1>Validación de certificados</h1>
            <p>
              Ingresa el código de siete dígitos que figura en el certificado.
            </p>
          </div>
          <form (ngSubmit)="verify()" class="filter-bar">
            <input
              name="codigo"
              [(ngModel)]="code"
              pattern="[0-9]{7}"
              minlength="7"
              maxlength="7"
              placeholder="Código de 7 dígitos"
              aria-label="Código del certificado"
              required
            /><button class="button">Verificar →</button>
          </form>
          @if (searched()) {
            @if (certificate(); as cert) {
              <section class="panel verified-card">
                <span class="stat-icon mint"
                  ><svg><use href="#i-check" /></svg></span
                ><span class="eyebrow">REGISTRO ENCONTRADO</span>
                <h2>Certificado válido</h2>
                <dl>
                  <dt>Participante</dt>
                  <dd>{{ cert.studentName }}</dd>
                  <dt>Programa</dt>
                  <dd>{{ cert.courseTitle }}</dd>
                  <dt>Código</dt>
                  <dd>{{ cert.code }}</dd>
                  <dt>Fecha de emisión</dt>
                  <dd>{{ date(cert.issuedAt) }}</dd>
                  <dt>Institución</dt>
                  <dd>Inudi Perú</dd>
                </dl>
                @if (auth.demo()) {
                  <div class="soft-note">
                    Registro de demostración local. No acredita una
                    certificación institucional real.
                  </div>
                }
              </section>
            } @else {
              <div class="alert error">
                No se encontró un certificado con ese código.
              </div>
            }
          }
        </div>
      }
    }
  `,
})
export class PortalComponent extends AsyncPage {
  view = this.route.snapshot.data["view"] as string;
  profile = inject(FormBuilder).nonNullable.group({
    fullName: ["", Validators.required],
    phone: [""],
    professionalTitle: [""],
    oldPassword: [""],
    newPassword: [""],
  });
  myCourses = signal<CourseRow[]>([]);
  cart = signal<{ id: number; course: Course }[]>([]);
  total = signal(0);
  payments = signal<Payment[]>([]);
  certificates = signal<Certificate[]>([]);
  comments = signal<Post[]>([]);
  certificate = signal<Certificate | null>(null);
  searched = signal(false);
  code = "";
  constructor() {
    super();
    this.watch(() => this.refresh());
  }
  async refresh(): Promise<void> {
    await this.load(async () => {
      switch (this.view) {
        case "profile": {
          const a = await this.api.get<Account>("/perfil");
          this.profile.patchValue({
            fullName: a.fullName,
            phone: a.phone || "",
            professionalTitle: a.professionalTitle || "",
            oldPassword: "",
            newPassword: "",
          });
          break;
        }
        case "courses":
          this.myCourses.set(await this.api.get<CourseRow[]>("/mis-cursos"));
          break;
        case "cart": {
          const d = await this.api.get<{
            cart: { id: number; course: Course }[];
            total: number;
          }>("/carrito");
          this.cart.set(d.cart);
          this.total.set(d.total);
          break;
        }
        case "payments":
          this.payments.set(await this.api.get<Payment[]>("/mis-pagos"));
          break;
        case "certificates": {
          const d = await this.api.get<{
            certificates: Certificate[];
            myCourses: CourseRow[];
          }>("/certificados");
          this.certificates.set(d.certificates);
          this.myCourses.set(d.myCourses);
          break;
        }
        case "comments":
          this.comments.set(await this.api.get<Post[]>("/comentarios"));
          break;
        case "verify": {
          this.code =
            this.route.snapshot.paramMap.get("code") ||
            this.route.snapshot.queryParamMap.get("codigo") ||
            "";
          const d = await this.api.get<{
            certificate: Certificate | null;
            searched: boolean;
          }>("/cert", this.code ? { codigo: this.code } : {});
          this.certificate.set(d.certificate);
          this.searched.set(d.searched);
          break;
        }
      }
    });
  }
  async saveProfile(): Promise<void> {
    this.profile.markAllAsTouched();
    if (this.profile.invalid) {
      this.error.set("Revisa los campos del formulario.");
      return;
    }
    await this.action(
      async () => {
        await this.api.send("PUT", "/perfil", this.profile.getRawValue());
        await this.auth.refresh();
      },
      () => this.refresh(),
      "Perfil actualizado.",
    );
  }
  async remove(id: number): Promise<void> {
    await this.action(
      async () => {
        await this.api.send("DELETE", "/carrito/" + id);
        await this.auth.refresh();
      },
      () => this.refresh(),
      "Curso retirado.",
    );
  }
  async checkout(): Promise<void> {
    await this.action(
      async () => {
        await this.api.send("POST", "/carrito/solicitar");
        await this.auth.refresh();
        await this.router.navigate(["/mis-pagos"]);
      },
      async () => {},
      "Solicitud registrada. La administración validará el pago y habilitará tu matrícula.",
    );
  }
  async issue(id: number): Promise<void> {
    await this.action(
      () => this.api.send("POST", "/certificados/emitir/" + id),
      () => this.refresh(),
      "Tu certificado está disponible para descargar y validar.",
    );
  }
  verify(): void {
    void this.router.navigate(["/cert"], {
      queryParams: { codigo: this.code },
    });
  }
}
