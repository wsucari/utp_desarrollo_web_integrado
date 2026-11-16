import { Component, inject, input, output, signal } from "@angular/core";
import { DecimalPipe } from "@angular/common";
import { RouterLink } from "@angular/router";
import { Api, Auth, failure } from "../core/api";
import { Course, CourseRow } from "../core/models";

@Component({
  selector: "inudi-card",
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  template: `
    <article
      class="tw-flex tw-h-full tw-min-h-[430px] tw-flex-col tw-overflow-hidden tw-rounded-lg tw-border tw-border-slate-200 tw-bg-white tw-shadow-sm hover:tw-shadow-md"
    >
      <a
        [routerLink]="['/cursos', course().id]"
        class="tw-relative tw-block tw-bg-slate-100"
        ><img
          src="/images/logos/inudiblack.png"
          [alt]="course().title"
          class="tw-aspect-video tw-w-full tw-object-cover"
        /><span
          class="tw-absolute tw-left-3 tw-top-3 tw-rounded-full tw-px-3 tw-py-1 tw-text-xs tw-font-bold tw-shadow-sm"
          [class]="status() === 'inscrito' ? 'tw-bg-[#0E2F66] tw-text-white' : status() === 'carrito' ? 'tw-bg-[#D4A017] tw-text-white' : 'tw-bg-white tw-text-[#0E2F66]'"
          >{{
            status() === "inscrito"
              ? "Inscrito"
              : status() === "carrito"
                ? "En carrito"
                : "Disponible"
          }}</span
        ></a
      >
      <div class="tw-flex tw-flex-1 tw-flex-col tw-p-4">
        <a
          [routerLink]="['/cursos', course().id]"
          class="tw-text-lg tw-font-bold tw-leading-snug tw-text-[#0E2F66]"
          >{{ course().title }}</a
        >
        <div class="tw-mt-2 tw-text-sm tw-text-[#64748B]">
          @if (modules() !== null) {
            {{ modules() }} módulos ·
          }
          {{ course().level }}
        </div>
        <p
          class="tw-mt-3 tw-line-clamp-2 tw-text-sm tw-leading-6 tw-text-[#64748B]"
        >
          {{ course().description }}
        </p>
        <div class="tw-mt-auto tw-pt-4">
          <div
            class="tw-mb-3 tw-flex tw-items-center tw-justify-between tw-gap-2"
          >
            <strong class="tw-text-lg tw-text-[#0E2F66]"
              >S/. {{ course().price | number: "1.2-2" }}</strong
            ><span
              class="tw-rounded-full tw-bg-slate-100 tw-px-2.5 tw-py-1 tw-text-xs tw-font-semibold tw-text-[#64748B]"
              >{{ course().category }}</span
            >
          </div>
          @if (status() === "carrito") {
            <a routerLink="/carrito" class="tw-block tw-rounded-md tw-bg-[#D4A017] tw-px-4 tw-py-2.5 tw-text-center tw-text-sm tw-font-bold tw-text-white"
              >Ir al carrito</a
            >
          } @else if (
            auth.user()?.role === "ESTUDIANTE" && status() !== "inscrito"
          ) {
            <div class="tw-grid tw-grid-cols-2 tw-gap-2">
              <a
                [routerLink]="['/cursos', course().id]"
                class="tw-rounded-md tw-border tw-border-slate-300 tw-px-3 tw-py-2.5 tw-text-center tw-text-sm tw-font-bold tw-text-[#0E2F66]"
                >Ver curso</a
              ><button
                type="button"
                class="tw-w-full tw-border-0 tw-rounded-md tw-bg-[#0E2F66] tw-px-3 tw-py-2.5 tw-text-sm tw-font-bold tw-text-white"
                [disabled]="busy()"
                (click)="add()"
              >
                Agregar
              </button>
            </div>
          } @else {
            <a
              [routerLink]="['/cursos', course().id]"
              class="tw-block tw-rounded-md tw-bg-[#0E2F66] tw-px-4 tw-py-2.5 tw-text-center tw-text-sm tw-font-bold tw-text-white"
              >{{
                status() === "inscrito" ? "Continuar curso" : "Ver curso"
              }}</a
            >
          }
          @if (error()) {
            <p role="alert" class="field-error">{{ error() }}</p>
          }
        </div>
      </div>
    </article>
  `,
})
export class CourseCardComponent {
  course = input.required<Course>();
  status = input("disponible");
  modules = input<number | null>(null);
  changed = output<void>();
  auth = inject(Auth);
  api = inject(Api);
  busy = signal(false);
  error = signal("");
  async add(): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    try {
      await this.api.send("POST", "/carrito/" + this.course().id);
      await this.auth.refresh();
      this.changed.emit();
    } catch (e) {
      this.error.set(failure(e).message);
    } finally {
      this.busy.set(false);
    }
  }
}

@Component({
  selector: "inudi-enrolled",
  standalone: true,
  imports: [RouterLink],
  template: `
    <div
      class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 xl:tw-grid-cols-3 tw-gap-5 tw-mt-4"
    >
      @for (row of rows(); track row.enrollment.id) {
        <article
          class="tw-bg-white tw-border tw-border-gray-200 tw-rounded-xl tw-overflow-hidden"
        >
          <img
            class="tw-w-full tw-aspect-video tw-object-cover tw-bg-gray-100"
            src="/images/logos/inudiblack.png"
            [alt]="row.course.title"
          />
          <div class="tw-p-5">
            <h3 class="tw-text-lg tw-font-semibold">{{ row.course.title }}</h3>
            <p class="tw-text-sm tw-text-gray-500">
              {{ row.moduleCount }} módulos · {{ row.contentCount }} contenidos
            </p>
            @if (row.nextContent && row.enrollment.current) {
              <p class="tw-text-sm">Siguiente: {{ row.nextContent.title }}</p>
            }
            <div class="tw-flex tw-justify-between tw-text-sm tw-mt-4">
              <span>Progreso</span><strong>{{ row.percent }}%</strong>
            </div>
            <progress
              class="tw-w-full"
              max="100"
              [value]="row.percent"
            ></progress>
            <p class="tw-text-gray-500 tw-text-sm tw-mt-2">
              Acceso hasta {{ row.enrollment.expiresAt.slice(0, 10) }}
            </p>
            @if (row.enrollment.current) {
              <a
                class="button"
                [routerLink]="
                  row.nextContent
                    ? ['/contenidos', row.nextContent.id]
                    : ['/aula/cursos', row.course.id]
                "
                >{{ row.percent > 0 ? "Continuar curso" : "Empezar curso" }}</a
              >
            } @else {
              <span class="badge warning">Acceso no vigente</span>
            }
          </div>
        </article>
      }
    </div>
    @if (!rows().length) {
      <div class="empty-state">
        <p>No tienes cursos inscritos.</p>
        <a routerLink="/" class="text-link">Explorar cursos</a>
      </div>
    }
  `,
})
export class EnrolledComponent {
  rows = input<CourseRow[]>([]);
}
