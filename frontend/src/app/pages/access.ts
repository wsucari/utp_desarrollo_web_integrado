import { Component, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { AsyncPage } from "../shared/async-page";
import { failure } from "../core/api";

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div
      [class]="
        register
          ? ''
          : 'login-frame tw-flex tw-flex-col tw-items-center tw-justify-center tw-bg-gray-200 tw-px-4 tw-py-6'
      "
    >
      <section
        [class]="
          register
            ? 'narrow panel'
            : 'tw-w-full tw-max-w-md tw-bg-white tw-rounded-lg tw-shadow-xl tw-px-6 tw-py-8'
        "
      >
        @if (!register) {
          <div class="tw-flex tw-justify-center tw-mb-6">
            <a routerLink="/login" aria-label="Inudi Perú"
              ><img
                src="/images/logos/inudiwhite.png"
                alt="Inudi Perú"
                class="tw-h-20 tw-w-auto"
            /></a>
          </div>
        } @else {
          <span class="eyebrow">EMPIEZA UN NUEVO CAMINO</span>
        }
        <h1
          [class]="
            register
              ? ''
              : 'tw-text-3xl tw-font-bold tw-text-gray-700 tw-text-center tw-mb-8'
          "
        >
          {{ register ? "Crea tu cuenta" : "Plataforma Inudi" }}
        </h1>
        @if (register) {
          <p class="muted">
            Tu espacio para aprender y participar en los programas de Inudi
            Perú.
          </p>
        }
        @if (error()) {
          <p role="alert" class="alert error">{{ error() }}</p>
        }
        @if (notice()) {
          <p role="status" class="alert success">{{ notice() }}</p>
        }
        <form [formGroup]="form" (ngSubmit)="submit()" class="stack-form">
          @if (register) {
            <label
              >Nombre completo<input
                formControlName="fullName"
                autocomplete="name"
                maxlength="120"
                required
            /></label>
          }
          <label for="login-email">Correo electrónico</label
          ><input
            id="login-email"
            type="email"
            formControlName="email"
            autocomplete="username"
            placeholder="Usuario"
            maxlength="180"
            required
          />
          @if (register) {
            <label
              >Teléfono (opcional)<input formControlName="phone" maxlength="30"
            /></label>
          }
          <label for="login-password">Contraseña</label>
          <div class="tw-relative">
            <input
              id="login-password"
              [type]="show() ? 'text' : 'password'"
              formControlName="password"
              [attr.autocomplete]="
                register ? 'new-password' : 'current-password'
              "
              placeholder="Contraseña"
              required
              class="tw-w-full tw-pr-12"
            /><button
              type="button"
              [attr.aria-label]="
                show() ? 'Ocultar contraseña' : 'Mostrar contraseña'
              "
              [attr.aria-pressed]="show()"
              (click)="show.set(!show())"
              class="tw-absolute tw-right-2 tw-top-1/2 -tw-translate-y-1/2 tw-border-0 tw-bg-transparent tw-text-gray-500"
            >
              <svg viewBox="0 0 24 24">
                <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12Z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
            </button>
          </div>
          @if (register) {
            <small
              >Al menos 10 caracteres con mayúsculas, minúsculas y
              números.</small
            >
          }
          <div class="tw-flex tw-justify-center tw-mt-6">
            <button
              [disabled]="saving()"
              class="tw-rounded-md tw-border-0 tw-bg-gray-800 tw-px-5 tw-py-2.5 tw-text-sm tw-font-semibold tw-text-white"
            >
              {{
                saving()
                  ? "Procesando…"
                  : register
                    ? "Crear cuenta"
                    : "Iniciar Sesión"
              }}
            </button>
          </div>
        </form>
        @if (register) {
          <a routerLink="/login" class="text-link section-space"
            >Ya tengo una cuenta</a
          >
        }
        @if (auth.demo() && !register) {
          <details class="tw-mt-6 tw-text-xs tw-text-gray-600">
            <summary>Accesos de demostración local</summary>
            <p>
              Administración: <code>admin&#64;inudi.local</code><br />Docente:
              <code>docente&#64;inudi.local</code><br />Estudiante:
              <code>estudiante&#64;inudi.local</code>
            </p>
            <p>Contraseña: <code>Inudi2026!Demo</code></p>
            <p>Datos ficticios. No utilizar estas cuentas en producción.</p>
          </details>
        }
      </section>
    </div>
  `,
})
export class AccessComponent extends AsyncPage {
  register = this.route.snapshot.data["view"] === "register";
  show = signal(false);
  form = inject(FormBuilder).nonNullable.group({
    fullName: [""],
    email: ["", [Validators.required, Validators.email]],
    phone: [""],
    password: ["", Validators.required],
  });
  constructor() {
    super();
    if (this.register)
      this.form.controls.fullName.addValidators([
        Validators.required,
        Validators.minLength(2),
      ]);
    if (this.route.snapshot.queryParamMap.has("salida"))
      this.notice.set("Sesión cerrada correctamente.");
  }
  async submit(): Promise<void> {
    if (this.saving()) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.error.set("Completa los campos e ingresa un correo válido.");
      return;
    }
    this.saving.set(true);
    this.error.set("");
    try {
      const f = this.form.getRawValue();
      await this.auth.refresh();
      if (this.register) {
        await this.api.send("POST", "/auth/registro", f);
        await this.router.navigate(["/login"]);
        this.flash.message.set(
          "Tu cuenta se creó. Inicia sesión para explorar los cursos.",
        );
      } else {
        await this.auth.login(f.email, f.password);
        const target = this.route.snapshot.queryParamMap.get("returnUrl");
        await this.router.navigateByUrl(
          target?.startsWith("/") && !target.startsWith("//")
            ? target
            : this.auth.home(),
        );
      }
    } catch (e) {
      this.error.set(failure(e).message);
    } finally {
      this.saving.set(false);
    }
  }
}
