import { Component, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { AsyncPage } from "../shared/async-page";
import { StatusComponent } from "../shared/status";
import { CourseCardComponent } from "../shared/cards";
import { Catalog } from "../core/models";

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink, StatusComponent, CourseCardComponent],
  template: `
    <inudi-status [loading]="loading()" [error]="error()" (retry)="refresh()" />
    @if (data(); as d) {
      <div class="tw-bg-slate-50 tw-text-slate-950">
        <section class="tw-border-b tw-border-slate-200 tw-bg-white">
          <div
            class="tw-mx-auto tw-grid tw-max-w-screen-xl tw-gap-8 tw-px-4 tw-py-8 sm:tw-px-6 lg:tw-grid-cols-[minmax(0,1fr)_320px] lg:tw-px-8"
          >
            <div>
              <p
                class="tw-text-sm tw-font-bold tw-uppercase tw-tracking-wide tw-text-[#D4A017]"
              >
                InudiClass
              </p>
              <h1
                class="tw-mt-2 tw-text-3xl md:tw-text-4xl tw-font-bold tw-leading-tight tw-text-[#0E2F66]"
              >
                ¿Qué deseas lograr hoy?
              </h1>
              <p
                class="tw-mt-3 tw-max-w-3xl tw-text-base tw-leading-7 tw-text-[#64748B]"
              >
                Explora formación especializada en investigación científica,
                tesis, publicación académica, inteligencia artificial,
                estadística y docencia universitaria.
              </p>
              <form (ngSubmit)="search()" class="tw-mt-6">
                <div
                  class="tw-flex tw-flex-col sm:tw-flex-row tw-gap-3 tw-rounded-lg tw-border tw-border-slate-200 tw-bg-slate-50 tw-p-2 tw-shadow-sm"
                >
                  <input
                    type="search"
                    name="search"
                    [(ngModel)]="filters.search"
                    placeholder="Buscar cursos, temas, metodologías o docentes"
                    aria-label="Buscar cursos"
                    maxlength="180"
                    class="tw-h-12 tw-w-full tw-flex-1 tw-rounded-md tw-border tw-border-transparent tw-bg-white tw-px-4 tw-text-sm"
                  /><button
                    class="tw-h-12 tw-rounded-md tw-bg-[#0E2F66] tw-px-6 tw-text-sm tw-font-bold tw-text-white tw-border-0"
                  >
                    Buscar
                  </button>
                </div>
              </form>
            </div>
            <div
              class="tw-rounded-lg tw-border tw-border-[#D4A017]/35 tw-bg-[#D4A017]/10 tw-p-5"
            >
              <p class="tw-text-sm tw-font-bold tw-text-[#0E2F66]">
                Aprendizaje orientado a resultados
              </p>
              <p class="tw-mt-2 tw-text-sm tw-leading-6 tw-text-[#64748B]">
                Elige una ruta académica y avanza desde el fundamento hasta la
                aplicación práctica en investigación y docencia.
              </p>
              <div class="tw-mt-4 tw-grid tw-grid-cols-2 tw-gap-3 tw-text-sm">
                <div class="tw-rounded-md tw-bg-white tw-p-3">
                  <strong class="tw-block tw-text-lg tw-text-[#0E2F66]">{{
                    d.accessCounts["todos"]
                  }}</strong
                  ><span>Cursos</span>
                </div>
                <div class="tw-rounded-md tw-bg-white tw-p-3">
                  <strong class="tw-block tw-text-lg tw-text-[#0E2F66]">{{
                    d.accessCounts["inscritos"]
                  }}</strong
                  ><span>Inscritos</span>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section
          class="tw-mx-auto tw-max-w-screen-xl tw-px-4 tw-py-8 sm:tw-px-6 lg:tw-px-8"
        >
          <h2 class="tw-text-2xl tw-font-bold tw-text-[#0E2F66]">
            Rutas académicas
          </h2>
          <p class="tw-mt-1 tw-text-sm tw-text-[#64748B]">
            Navega por objetivos reales de investigadores y docentes
            universitarios.
          </p>
          <div
            class="tw-mt-4 tw-grid tw-grid-cols-1 tw-gap-4 sm:tw-grid-cols-2 lg:tw-grid-cols-3 xl:tw-grid-cols-6"
          >
            @for (route of d.academicRoutes; track route.slug) {
              <a
                routerLink="/"
                [queryParams]="params('ruta', route.slug)"
                class="tw-block tw-rounded-lg tw-border tw-border-slate-200 tw-p-4 tw-bg-white hover:tw-border-[#1E4E9B]"
                ><h3 class="tw-text-sm tw-font-bold tw-text-[#0E2F66]">
                  {{ route.title }}
                </h3>
                <p class="tw-mt-2 tw-text-xs tw-leading-5 tw-text-[#64748B]">
                  {{ route.description }}
                </p></a
              >
            }
          </div>
        </section>
        @if (d.continueCourses.length) {
          <section
            class="tw-mx-auto tw-max-w-screen-xl tw-px-4 tw-pb-8 sm:tw-px-6 lg:tw-px-8"
          >
            <h2 class="tw-text-2xl tw-font-bold tw-text-[#0E2F66]">
              Continuar aprendiendo
            </h2>
            <p class="tw-text-sm tw-text-[#64748B]">
              Retoma los cursos en los que ya tienes acceso activo.
            </p>
            <div
              class="tw-mt-4 tw-grid tw-grid-cols-1 md:tw-grid-cols-2 xl:tw-grid-cols-4 tw-gap-5"
            >
              @for (c of d.continueCourses; track c.id) {
                <inudi-card
                  [course]="c"
                  [status]="d.courseInfo[c.id].status"
                  [modules]="d.courseInfo[c.id].modules"
                  (changed)="refresh()"
                />
              }
            </div>
          </section>
        }
        @if (d.featuredCourses.length) {
          <section
            class="tw-mx-auto tw-max-w-screen-xl tw-px-4 tw-pb-8 sm:tw-px-6 lg:tw-px-8"
          >
            <h2 class="tw-text-2xl tw-font-bold tw-text-[#0E2F66]">
              Cursos destacados
            </h2>
            <p class="tw-text-sm tw-text-[#64748B]">
              Programas relevantes para fortalecer tu producción académica.
            </p>
            <div
              class="tw-mt-4 tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-3 xl:tw-grid-cols-4 tw-gap-5"
            >
              @for (c of d.featuredCourses; track c.id) {
                <inudi-card
                  [course]="c"
                  [status]="d.courseInfo[c.id].status"
                  [modules]="d.courseInfo[c.id].modules"
                  (changed)="refresh()"
                />
              }
            </div>
          </section>
        }
        <section
          class="tw-mx-auto tw-max-w-screen-xl tw-px-4 tw-pb-10 sm:tw-px-6 lg:tw-px-8"
        >
          <div
            class="tw-flex tw-flex-wrap tw-items-end tw-justify-between tw-gap-4"
          >
            <div>
              <h2 class="tw-text-2xl tw-font-bold tw-text-[#0E2F66]">
                Todos los cursos
              </h2>
              <p>{{ d.courses.length }} resultados disponibles.</p>
            </div>
            <div class="tw-flex tw-flex-wrap tw-gap-2">
              <a
                routerLink="/"
                [queryParams]="params('ruta', '')"
                class="button button-ghost"
                >Todos</a
              >
              @for (r of d.academicRoutes; track r.slug) {
                <a
                  routerLink="/"
                  [queryParams]="params('ruta', r.slug)"
                  class="button button-ghost"
                  >{{ r.label }}</a
                >
              }
              <button
                type="button"
                (click)="more.set(!more())"
                [attr.aria-expanded]="more()"
                aria-controls="course-filters"
                class="button button-ghost"
              >
                Más filtros
              </button>
            </div>
          </div>
          <div class="tw-my-5 tw-flex tw-flex-wrap tw-gap-2">
            @for (key of ["todos", "inscritos", "disponibles"]; track key) {
              <a
                routerLink="/"
                [queryParams]="params('acceso', key)"
                class="button"
                [class.button-ghost]="filters.acceso !== key"
                >{{
                  key === "todos"
                    ? "Todos"
                    : key === "inscritos"
                      ? "Mis cursos"
                      : "No inscritos"
                }}
                {{ d.accessCounts[key] }}</a
              >
            }
          </div>
          @if (more()) {
            <form
              id="course-filters"
              (ngSubmit)="search()"
              class="panel section-space"
            >
              <h3>Filtros de cursos</h3>
              <div class="form-grid">
                <label
                  >Buscar<input
                    name="filterSearch"
                    [(ngModel)]="filters.search" /></label
                ><label
                  >Ruta académica<select name="ruta" [(ngModel)]="filters.ruta">
                    <option value="">Todas</option>
                    @for (r of d.academicRoutes; track r.slug) {
                      <option [value]="r.slug">{{ r.title }}</option>
                    }
                  </select></label
                ><label
                  >Vista<select name="acceso" [(ngModel)]="filters.acceso">
                    <option value="todos">Todos</option>
                    <option value="inscritos">Mis cursos</option>
                    <option value="disponibles">No inscritos</option>
                  </select></label
                ><label
                  >Categoría<select
                    name="categoria"
                    [(ngModel)]="filters.categoria"
                  >
                    <option value="">Todas</option>
                    @for (c of d.categories; track c) {
                      <option [value]="c">{{ c }}</option>
                    }
                  </select></label
                ><label
                  >Nivel<select name="nivel" [(ngModel)]="filters.nivel">
                    <option value="">Todos</option>
                    @for (l of d.levels; track l) {
                      <option [value]="l">{{ l }}</option>
                    }
                  </select></label
                >
              </div>
              <div class="actions">
                <a routerLink="/" class="text-link">Limpiar</a
                ><button class="button">Aplicar filtros</button>
              </div>
            </form>
          }
          <div
            class="tw-grid tw-grid-cols-1 tw-gap-5 md:tw-grid-cols-2 lg:tw-grid-cols-3 xl:tw-grid-cols-4 section-space"
          >
            @for (c of d.courses; track c.id) {
              <inudi-card
                [course]="c"
                [status]="d.courseInfo[c.id].status"
                [modules]="d.courseInfo[c.id].modules"
                (changed)="refresh()"
              />
            }
          </div>
          @if (!d.courses.length) {
            <div class="empty-state">
              <h3>No encontramos cursos con esos criterios</h3>
              <p>
                Prueba otra palabra clave o limpia los filtros para revisar todo
                el catálogo.
              </p>
              <a routerLink="/" class="button">Ver todo el catálogo</a>
            </div>
          }
        </section>
      </div>
    }
  `,
})
export class CatalogComponent extends AsyncPage {
  data = signal<Catalog | null>(null);
  more = signal(false);
  filters: Record<string, string> = {
    search: "",
    ruta: "",
    categoria: "",
    nivel: "",
    acceso: "todos",
  };
  constructor() {
    super();
    this.watch(() => this.refresh());
  }
  async refresh(): Promise<void> {
    await this.load(async () => {
      const q = this.route.snapshot.queryParams;
      this.filters = {
        search: q["search"] || q["q"] || "",
        ruta: q["ruta"] || "",
        categoria: q["categoria"] || q["category"] || "",
        nivel: q["nivel"] || "",
        acceso: q["acceso"] || "todos",
      };
      this.data.set(await this.api.get<Catalog>("/cursos", this.filters));
    });
  }
  params(key: string, value: string): Record<string, string> {
    return { ...this.filters, [key]: value };
  }
  search(): void {
    void this.router.navigate(["/"], { queryParams: this.filters });
  }
}
