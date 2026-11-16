import { Component, HostListener, inject, signal } from "@angular/core";
import {
  Router,
  RouterLink,
  RouterOutlet,
  NavigationEnd,
  NavigationStart,
} from "@angular/router";
import { filter } from "rxjs";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Auth, failure } from "./core/api";
import { Flash } from "./core/flash";

@Component({
  selector: "inudi-icons",
  standalone: true,
  templateUrl: "./shared/icons.html",
})
export class IconsComponent {}

@Component({
  selector: "inudi-app",
  standalone: true,
  imports: [RouterLink, RouterOutlet, IconsComponent],
  template: `
    <inudi-icons /><a class="skip-link" href="#main">Ir al contenido</a>
    <div class="tw-min-h-screen tw-flex tw-flex-col">
      <header
        id="site-header"
        class="tw-sticky tw-top-0 tw-z-50 tw-bg-[#042354] tw-text-white tw-shadow-sm"
      >
        <div
          class="tw-flex tw-h-16 tw-items-center tw-justify-between tw-gap-3 tw-px-4 md:tw-px-6"
        >
          <a
            routerLink="/"
            aria-label="Inudi Perú"
            class="tw-flex tw-items-center tw-gap-3 tw-shrink-0"
            ><img
              src="/images/logos/inudi_white.webp"
              alt=""
              class="tw-h-9 tw-w-auto"
            /><span class="tw-font-semibold tw-text-lg md:tw-text-xl"
              >Inudi Perú</span
            ></a
          >
          <div id="desktop-account" class="tw-hidden md:tw-block">
            @if (auth.user(); as user) {
              <div class="tw-relative account-menu">
                <button
                  id="desktop-account-button"
                  type="button"
                  [attr.aria-expanded]="desktopAccount()"
                  aria-controls="desktop-account-dropdown"
                  (click)="desktopAccount.set(!desktopAccount())"
                  class="tw-flex tw-items-center tw-gap-2 tw-bg-transparent tw-border-0 tw-p-2 tw-text-current"
                >
                  <span class="avatar">{{ user.initials }}</span><span>{{
                    user.fullName
                  }}</span
                  ><span aria-hidden="true">⌄</span>
                </button>
                @if (desktopAccount()) {
                  <div
                    id="desktop-account-dropdown"
                    class="tw-absolute tw-right-0 tw-mt-2 tw-w-48 tw-z-50 tw-rounded-md tw-bg-white tw-shadow-lg tw-text-gray-700"
                  >
                    <a
                      [routerLink]="profileLink"
                      class="tw-block tw-px-4 tw-py-2 tw-text-sm hover:tw-bg-gray-100"
                      >{{
                        user.role === "DOCENTE" ? "Mi perfil" : "Perfil"
                      }}</a><button
                      type="button"
                      (click)="logout()"
                      class="tw-w-full tw-text-left tw-border-0 tw-bg-transparent tw-px-4 tw-py-2 tw-text-sm hover:tw-bg-gray-100"
                    >
                      Cerrar Sesión
                    </button>
                  </div>
                }
              </div>
            } @else {
              <a routerLink="/login" class="tw-text-sm tw-font-semibold"
                >Iniciar Sesión</a
              >
            }
          </div>
          <button
            id="navigation-toggle"
            type="button"
            [attr.aria-expanded]="navigation()"
            aria-controls="sidebar"
            [attr.aria-label]="navigation() ? 'Cerrar menú' : 'Abrir menú'"
            (click)="toggleNavigation()"
            class="md:tw-hidden tw-flex tw-items-center tw-justify-center tw-w-10 tw-h-10 tw-border-0 tw-rounded-md tw-bg-transparent tw-text-white hover:tw-bg-white/10"
          >
            <svg aria-hidden="true" viewBox="0 0 24 24">
              <path d="M4 6h16 M4 12h16 M4 18h16" />
            </svg>
          </button>
        </div>
      </header>
      <div class="tw-flex tw-flex-1">
        @if (navigation()) {
          <button
            id="navigation-backdrop"
            type="button"
            aria-label="Cerrar menú"
            tabindex="-1"
            (click)="closeNavigation(true)"
            class="tw-fixed tw-inset-0 tw-top-16 tw-z-30 tw-bg-black/40 tw-border-0 md:tw-hidden"
          ></button>
        }
        <aside
          id="sidebar"
          aria-label="Menú lateral"
          [class.tw-hidden]="!navigation()"
          [class.reference-open]="navigation()"
          [class.md:tw-block]="!!auth.user()"
          class="tw-w-64 tw-shrink-0 tw-bg-white tw-border-r tw-border-gray-200 tw-p-3"
        >
          <nav aria-label="Menú lateral" class="tw-space-y-1">
            @for (link of links; track link.path) {
              <a
                [routerLink]="link.path === '#' ? null : link.path"
                [attr.href]="link.path === '#' ? '#' : null"
                (click)="link.path === '#' ? $event.preventDefault() : null"
                [attr.aria-current]="isActive(link.path) ? 'page' : null"
                class="sidebar-link tw-flex tw-items-center tw-gap-3 tw-p-3 tw-text-sm tw-font-medium"
                [class.is-current]="isActive(link.path)"
                ><svg aria-hidden="true">
                  <use [attr.href]="'#i-' + link.icon" /></svg
                >{{ link.label }}</a
              >
            }
            @if (auth.user()?.role === "ESTUDIANTE") {
              <a
                href="#"
                (click)="$event.preventDefault()"
                class="tw-flex tw-items-center tw-gap-3 tw-p-3"
                ><svg><use href="#i-settings" /></svg>Ajustes</a
              >
            }
            @if (auth.user()?.role !== "ADMIN") {
              <div class="tw-border-t tw-border-gray-200 tw-mt-4 tw-pt-3">
                <a
                  routerLink="/"
                  class="tw-flex tw-items-center tw-gap-3 tw-p-3"
                  ><svg><use href="#i-book" /></svg>Cursos Online</a
                >
                @if (auth.user()?.role !== "DOCENTE") {
                  <a routerLink="/nosotros" class="tw-block tw-p-3">Nosotros</a>
                }
                <a routerLink="/revistas" class="tw-block tw-p-3">Revistas</a
                ><a
                  href="https://editorial.inudi.edu.pe/index.php/editorialinudi/catalog"
                  class="tw-block tw-p-3"
                  >Libros</a
                ><a routerLink="/" class="tw-block tw-p-3">Inudi Plus</a>
              </div>
            }
            @if (auth.user() && auth.user()?.role !== "ADMIN") {
              <button
                type="button"
                (click)="logout()"
                class="tw-flex tw-items-center tw-gap-3 tw-w-full tw-p-3 tw-text-left tw-text-gray-700 tw-border-0 tw-bg-transparent hover:tw-bg-gray-100"
              >
                <svg><use href="#i-logout" /></svg>Salir
              </button>
            }
          </nav>
          <div id="mobile-account" class="md:tw-hidden sidebar-account">
            @if (auth.user(); as user) {
              <div class="tw-relative account-menu">
                <button
                  id="mobile-account-button"
                  type="button"
                  [attr.aria-expanded]="mobileAccount()"
                  aria-controls="mobile-account-dropdown"
                  (click)="mobileAccount.set(!mobileAccount())"
                  class="tw-flex tw-items-center tw-gap-2 tw-w-full tw-bg-transparent tw-border-0 tw-p-2"
                >
                  <span class="avatar">{{ user.initials }}</span><span
                    class="tw-break-words tw-text-left"
                    >{{ user.fullName }}</span
                  ><span aria-hidden="true">⌄</span>
                </button>
                @if (mobileAccount()) {
                  <div
                    id="mobile-account-dropdown"
                    class="account-dropup tw-absolute tw-left-0 tw-w-full tw-z-50 tw-bg-white tw-shadow-lg tw-text-gray-700"
                  >
                    <a
                      [routerLink]="profileLink"
                      class="tw-block tw-px-4 tw-py-2"
                      >{{
                        user.role === "DOCENTE" ? "Mi perfil" : "Perfil"
                      }}</a><button
                      type="button"
                      (click)="logout()"
                      class="tw-w-full tw-text-left tw-border-0 tw-bg-transparent tw-px-4 tw-py-2"
                    >
                      Cerrar Sesión
                    </button>
                  </div>
                }
              </div>
            } @else {
              <a
                routerLink="/login"
                class="tw-block tw-p-3 tw-font-semibold tw-text-[#042354]"
                >Iniciar Sesión</a
              >
            }
          </div>
        </aside>
        <main
          id="main"
          class="reference-main tw-min-w-0 tw-flex-1"
          [class.tw-p-0]="path() === '/'"
          [class.tw-p-4]="path() !== '/'"
          [class.md:tw-p-6]="path() !== '/'"
        >
          @if (error()) {
            <div role="alert" class="alert error">{{ error() }}</div>
          }
          @if (flash.message()) {
            <div role="status" class="alert success">{{ flash.message() }}</div>
          }
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class AppComponent {
  auth = inject(Auth);
  router = inject(Router);
  flash = inject(Flash);
  path = signal("/");
  navigation = signal(false);
  desktopAccount = signal(false);
  mobileAccount = signal(false);
  error = signal("");
  constructor() {
    this.router.events.pipe(takeUntilDestroyed()).subscribe((event) => {
      if (event instanceof NavigationStart) this.flash.message.set("");
    });
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => {
        this.path.set(this.router.url.split("?")[0]);
        this.closeNavigation();
      });
  }
  get profileLink(): string {
    return this.auth.user()?.role === "DOCENTE"
      ? "/profesor/perfil"
      : this.auth.user()?.role === "ESTUDIANTE"
        ? "/users/panel/perfil"
        : "/perfil";
  }
  get links(): { path: string; label: string; icon: string }[] {
    if (this.auth.user()?.role === "ADMIN")
      return [
        { path: "/dashboard", label: "Inicio", icon: "home" },
        { path: "/admin/cursos", label: "Cursos", icon: "book" },
        { path: "/admin/user", label: "Usuarios", icon: "users" },
        { path: "/admin/pagos", label: "Pagos", icon: "wallet" },
        { path: "/admin/resenas", label: "Reseñas", icon: "comments" },
        { path: "/admin/profesores", label: "Profesores", icon: "user" },
        { path: "/admin/vista-previa", label: "Ver como", icon: "grid" },
        { path: "/admin/sesiones", label: "Sesiones", icon: "chart" },
      ];
    if (this.auth.user()?.role === "DOCENTE")
      return [
        { path: "/profesor/dashboard", label: "Inicio", icon: "home" },
        { path: "/profesor/cursos", label: "Mis cursos", icon: "book" },
        { path: "/profesor/perfil", label: "Mi perfil", icon: "user" },
      ];
    if (this.auth.user()?.role === "ESTUDIANTE")
      return [
        { path: "/users/panel/inicio", label: "Inicio", icon: "home" },
        { path: "/users/panel/perfil", label: "Mi perfil", icon: "user" },
        {
          path: "/users/panel/inscritos",
          label: "Cursos Inscritos",
          icon: "book",
        },
        { path: "#", label: "Lista de deseos", icon: "heart" },
        {
          path: "/users/panel/comentarios",
          label: "Mis comentarios",
          icon: "comments",
        },
        { path: "/users/carrito", label: "Carritos", icon: "cart" },
      ];
    return [];
  }
  isActive(path: string): boolean {
    const current = this.path();
    if (path === "/admin/cursos" || path === "/profesor/cursos")
      return current.startsWith(path) || current.startsWith("/gestion");
    return path !== "#" && current === path;
  }
  toggleNavigation(): void {
    this.navigation.set(!this.navigation());
    document.body.classList.toggle("navigation-open", this.navigation());
    if (this.navigation())
      setTimeout(() =>
        document.querySelector<HTMLElement>("#sidebar a")?.focus(),
      );
  }
  closeNavigation(focus = false): void {
    this.navigation.set(false);
    this.desktopAccount.set(false);
    this.mobileAccount.set(false);
    document.body.classList.remove("navigation-open");
    if (focus) document.getElementById("navigation-toggle")?.focus();
  }
  @HostListener("window:resize") resize(): void {
    if (window.innerWidth >= 768) this.closeNavigation();
  }
  @HostListener("document:click", ["$event"]) click(event: MouseEvent): void {
    if (!(event.target as HTMLElement).closest(".account-menu")) {
      this.desktopAccount.set(false);
      this.mobileAccount.set(false);
    }
  }
  @HostListener("document:keydown", ["$event"]) key(
    event: KeyboardEvent,
  ): void {
    if (event.key === "Escape") {
      if (this.desktopAccount() || this.mobileAccount()) {
        const id = this.mobileAccount()
          ? "mobile-account-button"
          : "desktop-account-button";
        this.desktopAccount.set(false);
        this.mobileAccount.set(false);
        document.getElementById(id)?.focus();
      } else this.closeNavigation(true);
    }
    if (event.key === "Tab" && this.navigation()) {
      const controls = Array.from(
        document.querySelectorAll<HTMLElement>(
          "#navigation-toggle,#sidebar a[href],#sidebar button",
        ),
      ).filter((e) => e.getClientRects().length);
      const first = controls[0],
        last = controls.at(-1);
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last?.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first?.focus();
      }
    }
  }
  async logout(): Promise<void> {
    try {
      await this.auth.logout();
      await this.router.navigate(["/login"], { queryParams: { salida: "" } });
    } catch (e) {
      this.error.set(failure(e).message);
    }
  }
}
