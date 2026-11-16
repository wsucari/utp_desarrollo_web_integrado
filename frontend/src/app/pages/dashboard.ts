import { Component, signal } from "@angular/core";
import { DecimalPipe } from "@angular/common";
import { RouterLink } from "@angular/router";
import { AsyncPage } from "../shared/async-page";
import { StatusComponent } from "../shared/status";
import { EnrolledComponent } from "../shared/cards";
import { Dashboard } from "../core/models";

@Component({
  standalone: true,
  imports: [DecimalPipe, RouterLink, StatusComponent, EnrolledComponent],
  template: `
    <inudi-status [loading]="loading()" [error]="error()" (retry)="refresh()" />
    @if (data(); as d) {
      @switch (auth.user()?.role) {
        @case ("ADMIN") {
          <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800 tw-mb-8">
            Panel de Administración
          </h1>
          <div
            class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-4 tw-gap-6"
          >
            <div class="tw-bg-white tw-p-6 tw-rounded-lg tw-shadow-lg">
              <h2>Cursos</h2>
              <p class="muted">Total cursos</p>
              <strong class="tw-block tw-mt-6 tw-text-4xl tw-text-blue-500">{{
                d.courseCount
              }}</strong>
            </div>
            <div class="tw-bg-white tw-p-6 tw-rounded-lg tw-shadow-lg">
              <h2>Profesores</h2>
              <p class="muted">Registrados</p>
              <strong class="tw-block tw-mt-6 tw-text-4xl tw-text-green-500">{{
                d.teacherCount
              }}</strong>
            </div>
            <div class="tw-bg-white tw-p-6 tw-rounded-lg tw-shadow-lg">
              <h2>Usuarios</h2>
              <p class="muted">Total activos</p>
              <strong class="tw-block tw-mt-6 tw-text-4xl tw-text-yellow-500">{{
                d.users
              }}</strong>
            </div>
            <div class="tw-bg-white tw-p-6 tw-rounded-lg tw-shadow-lg">
              <h2>Pagos</h2>
              <p class="muted">{{ d.paymentCount }} Realizados</p>
              <strong class="tw-block tw-mt-6 tw-text-3xl tw-text-red-500"
                >S/. {{ d.paymentTotal | number: "1.2-2" }}</strong
              >
            </div>
          </div>
          <section class="tw-mt-12">
            <h2>Detalle de Cursos Registrados</h2>
            <div
              class="table-wrap tw-bg-white tw-rounded-lg tw-shadow-lg"
              tabindex="0"
              role="region"
              aria-label="Detalle de cursos"
            >
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Curso</th>
                    <th>Profesor</th>
                    <th>Usuarios inscritos</th>
                    <th>Total Pagado</th>
                    <th>Certificados Emitidos</th>
                    <th>Reseñas</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  @for (row of d.courseRows; track row.course.id) {
                    <tr>
                      <td>{{ row.course.id }}</td>
                      <td>
                        <a [routerLink]="['/gestion/cursos', row.course.id]">{{
                          row.course.title
                        }}</a>
                      </td>
                      <td>{{ row.course.teacherNames }}</td>
                      <td>{{ row.enrollments }}</td>
                      <td>S/. {{ row.payments | number: "1.2-2" }}</td>
                      <td>{{ row.certificates }}</td>
                      <td>{{ row.comments }}</td>
                      <td>
                        {{ row.course.published ? "Publicado" : "Borrador" }}
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </section>
        }
        @case ("DOCENTE") {
          <h1 class="tw-text-2xl tw-font-semibold">Menú principal</h1>
          <div
            class="tw-mt-6 tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-4 tw-gap-4"
          >
            <div
              class="tw-p-4 tw-bg-white tw-shadow-lg tw-rounded-lg tw-flex tw-items-center tw-gap-4"
            >
              <svg class="tw-text-blue-500"><use href="#i-book" /></svg>
              <div>
                <p class="tw-text-xl tw-font-semibold tw-mb-0">
                  {{ d.courseCount }}
                </p>
                <p class="tw-text-gray-600 tw-mb-0">Cursos asignados</p>
              </div>
            </div>
          </div>
          <section class="tw-mt-8">
            <h2 class="tw-text-3xl tw-font-bold tw-text-gray-900 tw-mb-6">
              Cursos asignados
            </h2>
            <div
              class="tw-grid tw-grid-cols-1 sm:tw-grid-cols-2 lg:tw-grid-cols-3 xl:tw-grid-cols-4 tw-gap-6"
            >
              @for (course of d.courses; track course.id) {
                <article class="tw-p-4 tw-bg-white tw-rounded-xl tw-shadow-lg">
                  <img
                    src="/images/logos/inudiblack.png"
                    [alt]="course.title"
                    class="tw-w-full tw-aspect-video tw-object-cover tw-rounded-xl tw-bg-gray-100"
                  />
                  <h3
                    class="tw-text-xl tw-font-semibold tw-text-gray-800 tw-mt-4"
                  >
                    {{ course.title }}
                  </h3>
                  <div class="tw-grid tw-gap-2 tw-mt-4">
                    <a [routerLink]="['/cursos', course.id]" class="button"
                      >Ver curso</a
                    ><a
                      [routerLink]="['/gestion/cursos', course.id]"
                      class="button"
                      >Gestionar contenido</a
                    >
                  </div>
                </article>
              }
            </div>
          </section>
        }
        @default {
          <h1 class="tw-text-2xl tw-font-semibold">Inicio</h1>
          <section
            class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200 tw-p-6 tw-mt-6"
          >
            <div
              class="tw-flex tw-flex-wrap tw-items-center tw-justify-between tw-gap-4"
            >
              <div>
                <h2>Hola, {{ auth.user()?.fullName }}</h2>
                <p class="tw-text-gray-600">
                  Tienes {{ d.activeCount }} cursos activos y
                  {{ d.totalStars }} estrellas acumuladas por tu avance.
                </p>
              </div>
              <div class="actions">
                <a routerLink="/users/panel/inscritos" class="button"
                  >Ver mis cursos</a
                ><a routerLink="/" class="button button-ghost"
                  >Explorar cursos</a
                >
              </div>
            </div>
          </section>
          <div
            class="tw-grid tw-grid-cols-2 lg:tw-grid-cols-4 tw-gap-4 tw-my-6"
          >
            <div class="tw-bg-white tw-rounded-lg tw-p-4">
              <p>Cursos inscritos</p>
              <strong class="tw-text-2xl">{{ d.courseCount }}</strong>
            </div>
            <div class="tw-bg-white tw-rounded-lg tw-p-4">
              <p>Cursos activos</p>
              <strong class="tw-text-2xl">{{ d.activeCount }}</strong>
            </div>
            <div class="tw-bg-white tw-rounded-lg tw-p-4">
              <p>Completados</p>
              <strong class="tw-text-2xl">{{ d.completedCount }}</strong>
            </div>
            <div class="tw-bg-white tw-rounded-lg tw-p-4">
              <p>Certificados</p>
              <strong class="tw-text-2xl">{{ d.certificateCount }}</strong>
            </div>
          </div>
          <h2>Mis cursos</h2>
          <inudi-enrolled [rows]="d.myCourses" />
        }
      }
    }
  `,
})
export class DashboardComponent extends AsyncPage {
  data = signal<Dashboard | null>(null);
  constructor() {
    super();
    this.watch(() => this.refresh());
  }
  async refresh(): Promise<void> {
    await this.load(async () =>
      this.data.set(await this.api.get<Dashboard>("/panel")),
    );
  }
}
