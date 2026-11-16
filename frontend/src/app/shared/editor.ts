import { ChangeDetectorRef, Component, ElementRef, ViewChild, inject, signal } from "@angular/core";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { failure } from "../core/api";

export interface Field {
  name: string;
  label: string;
  type?:
    | "text"
    | "email"
    | "password"
    | "number"
    | "date"
    | "url"
    | "textarea"
    | "select"
    | "checkbox"
    | "checklist"
    | "file";
  required?: boolean;
  min?: number;
  max?: number;
  maxLength?: number;
  minLength?: number;
  step?: string;
  options?: { value: string | number; label: string }[];
  disabled?: boolean;
  help?: string;
  accept?: string;
}
export interface EditorConfig {
  title: string;
  fields: Field[];
  initial?: Record<string, unknown>;
  help?: string;
  button?: string;
  save: (value: Record<string, unknown>) => Promise<unknown>;
}
@Component({
  selector: "inudi-editor",
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <dialog
      #dialog
      class="editor-modal"
      (cancel)="cancel($event)"
      (click)="backdrop($event)"
    >
      <div class="editor-modal-header">
        <h2 id="editor-dialog-title">{{ config?.title }}</h2>
        <button
          type="button"
          class="icon-button"
          aria-label="Cerrar formulario"
          [disabled]="busy()"
          (click)="close()"
        >
          ✕
        </button>
      </div>
      <div class="editor-modal-body">
        @if (error()) {
          <div role="alert" class="alert error">{{ error() }}</div>
        }
        @if (config?.help) {
          <p class="muted small">{{ config!.help }}</p>
        }
        <form [formGroup]="form" (ngSubmit)="submit()" class="stack-form">
          @for (field of fields(); track field.name) {
            @if (field.type === "checkbox") {
              <label class="checkbox-label"
                ><input type="checkbox" [formControlName]="field.name" />{{
                  field.label
                }}</label
              >
            } @else if (field.type === "checklist") {
              <fieldset class="teacher-options">
                <legend>{{ field.label }}</legend>
                @for (option of field.options; track option.value) {
                  <label class="checkbox-label"
                    ><input
                      type="checkbox"
                      [checked]="selected(field.name, option.value)"
                      (change)="toggle(field.name, option.value, $event)"
                    />{{ option.label }}</label
                  >
                }
              </fieldset>
            } @else {
              <label [for]="'editor-' + field.name"
                >{{ field.label }}
                @switch (field.type) {
                  @case ("textarea") {
                    <textarea
                      [id]="'editor-' + field.name"
                      [formControlName]="field.name"
                      rows="4"
                      [attr.maxlength]="field.maxLength"
                      [required]="!!field.required"
                    ></textarea>
                  }
                  @case ("select") {
                    <select
                      [id]="'editor-' + field.name"
                      [formControlName]="field.name"
                      [required]="!!field.required"
                    >
                      @for (option of field.options; track option.value) {
                        <option [ngValue]="option.value">
                          {{ option.label }}
                        </option>
                      }
                    </select>
                  }
                  @case ("file") {
                    <input
                      [id]="'editor-' + field.name"
                      type="file"
                      [attr.accept]="
                        field.accept ||
                        '.pdf,.docx,.xlsx,.pptx,.txt,.png,.jpg,.jpeg,.zip'
                      "
                      [required]="!!field.required"
                      (change)="file(field.name, $event)"
                    />
                  }
                  @default {
                    <input
                      [id]="'editor-' + field.name"
                      [type]="field.type || 'text'"
                      [formControlName]="field.name"
                      [attr.min]="field.min"
                      [attr.max]="field.max"
                      [attr.maxlength]="field.maxLength"
                      [attr.minlength]="field.minLength"
                      [attr.step]="field.step"
                      [required]="!!field.required"
                      [attr.autocomplete]="
                        field.type === 'password' ? 'new-password' : null
                      "
                    />
                  }
                }
              </label>
            }
            @if (field.help) {
              <small>{{ field.help }}</small>
            }
            @if (
              form.controls[field.name]?.invalid &&
              form.controls[field.name]?.touched
            ) {
              <small class="field-error" role="alert">{{
                fieldError(field.name)
              }}</small>
            }
          }
          <div class="actions">
            <button class="button" [disabled]="busy()">
              {{
                busy() ? "Guardando…" : config?.button || "Guardar cambios"
              }}</button
            ><button
              type="button"
              class="button button-ghost"
              [disabled]="busy()"
              (click)="close()"
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </dialog>
  `,
})
export class EditorComponent {
  private changeDetector = inject(ChangeDetectorRef);
  @ViewChild("dialog", { static: true }) dialog!: ElementRef<HTMLDialogElement>;
  fields = signal<Field[]>([]);
  busy = signal(false);
  error = signal("");
  config?: EditorConfig;
  form = new FormGroup<Record<string, FormControl>>({});
  private previous?: HTMLElement;
  open(config: EditorConfig): void {
    this.config = config;
    this.error.set("");
    const controls: Record<string, FormControl> = {};
    for (const f of config.fields) {
      const validators = [];
      if (f.required) validators.push(Validators.required);
      if (f.type === "email") validators.push(Validators.email);
      if (f.min !== undefined) validators.push(Validators.min(f.min));
      if (f.max !== undefined) validators.push(Validators.max(f.max));
      if (f.maxLength) validators.push(Validators.maxLength(f.maxLength));
      if (f.minLength) validators.push(Validators.minLength(f.minLength));
      controls[f.name] = new FormControl(
        {
          value:
            config.initial?.[f.name] ??
            (f.type === "checkbox"
              ? false
              : f.type === "checklist"
                ? []
                : f.type === "file"
                  ? null
                  : ""),
          disabled: !!f.disabled,
        },
        validators,
      );
    }
    this.form = new FormGroup(controls);
    this.fields.set([...config.fields]);
    this.changeDetector.detectChanges();
    this.previous = document.activeElement as HTMLElement;
    this.dialog.nativeElement.setAttribute(
      "aria-labelledby",
      "editor-dialog-title",
    );
    if (!this.dialog.nativeElement.open) this.dialog.nativeElement.showModal();
  }
  selected(name: string, value: string | number): boolean {
    return (this.form.controls[name].value as unknown[]).includes(value);
  }
  toggle(name: string, value: string | number, event: Event): void {
    const current = this.form.controls[name].value as unknown[];
    this.form.controls[name].setValue(
      (event.target as HTMLInputElement).checked
        ? [...current, value]
        : current.filter((v) => v !== value),
    );
  }
  file(name: string, event: Event): void {
    this.form.controls[name].setValue(
      (event.target as HTMLInputElement).files?.[0] || null,
    );
  }
  close(): void {
    if (this.busy()) return;
    this.dialog.nativeElement.close();
    this.previous?.focus();
  }
  cancel(event: Event): void {
    if (this.busy()) event.preventDefault();
  }
  backdrop(event: MouseEvent): void {
    if (event.target === this.dialog.nativeElement) {
      const r = this.dialog.nativeElement.getBoundingClientRect();
      if (
        event.clientX < r.left ||
        event.clientX > r.right ||
        event.clientY < r.top ||
        event.clientY > r.bottom
      )
        this.close();
    }
  }
  fieldError(name: string): string {
    const e = this.form.controls[name].errors;
    return (
      e?.["server"] ||
      (e?.["required"]
        ? "Este campo es obligatorio."
        : e?.["email"]
          ? "Ingresa un correo válido."
          : "Revisa el formato y los límites de este campo.")
    );
  }
  async submit(): Promise<void> {
    if (this.busy() || !this.config) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    this.busy.set(true);
    this.error.set("");
    try {
      await this.config.save(this.form.getRawValue());
      this.busy.set(false);
      this.close();
    } catch (error) {
      const e = failure(error);
      this.error.set(e.message);
      for (const [name, message] of Object.entries(e.errors))
        this.form.controls[name]?.setErrors({ server: message });
    } finally {
      this.busy.set(false);
    }
  }
}
