import { inject } from "@angular/core";
import { Routes, Router, RedirectFunction } from "@angular/router";
import { accessGuard } from "./core/api";
const dashboard = () =>
  import("./pages/dashboard").then((m) => m.DashboardComponent);
const admin = () => import("./pages/admin").then((m) => m.AdminComponent);
const management = () =>
  import("./pages/management").then((m) => m.ManagementComponent);
const portal = () => import("./pages/portal").then((m) => m.PortalComponent);
const learning = () =>
  import("./pages/learning").then((m) => m.LearningComponent);
const course = () => import("./pages/course").then((m) => m.CourseComponent);
const catalogAlias: RedirectFunction = ({ queryParams }) =>
  inject(Router).createUrlTree(["/"], { queryParams });
const adminOnly = { roles: ["ADMIN"] };
const manager = { roles: ["ADMIN", "DOCENTE"] };
const student = { roles: ["ESTUDIANTE"] };
export const routes: Routes = [
  {
    path: "",
    pathMatch: "full",
    loadComponent: () =>
      import("./pages/catalog").then((m) => m.CatalogComponent),
  },
  {
    path: "login",
    loadComponent: () =>
      import("./pages/access").then((m) => m.AccessComponent),
    data: { view: "login" },
  },
  {
    path: "registro",
    loadComponent: () =>
      import("./pages/access").then((m) => m.AccessComponent),
    data: { view: "register" },
  },
  { path: "catalogo", pathMatch: "full", redirectTo: catalogAlias },
  { path: "cursos", pathMatch: "full", redirectTo: catalogAlias },
  { path: "catalogo/:id", redirectTo: "cursos/:id" },
  { path: "cursos/:id", loadComponent: course },
  ...["panel", "dashboard", "profesor/dashboard", "users/panel/inicio"].map(
    (path) => ({
      path,
      loadComponent: dashboard,
      canActivate: [accessGuard],
      data:
        path === "dashboard"
          ? adminOnly
          : path.startsWith("profesor")
            ? { roles: ["DOCENTE"] }
            : path.startsWith("users")
              ? student
              : {},
    }),
  ),
  ...["perfil", "profesor/perfil", "users/panel/perfil"].map((path) => ({
    path,
    loadComponent: portal,
    canActivate: [accessGuard],
    data: {
      view: "profile",
      ...(path.startsWith("profesor")
        ? { roles: ["DOCENTE"] }
        : path.startsWith("users")
          ? student
          : {}),
    },
  })),
  ...["mis-cursos", "users/panel/inscritos"].map((path) => ({
    path,
    loadComponent: portal,
    canActivate: [accessGuard],
    data: { view: "courses", ...student },
  })),
  ...["carrito", "users/carrito"].map((path) => ({
    path,
    loadComponent: portal,
    canActivate: [accessGuard],
    data: { view: "cart", ...student },
  })),
  {
    path: "mis-pagos",
    loadComponent: portal,
    canActivate: [accessGuard],
    data: { view: "payments", ...student },
  },
  {
    path: "certificados",
    loadComponent: portal,
    canActivate: [accessGuard],
    data: { view: "certificates", ...student },
  },
  {
    path: "cert",
    loadComponent: portal,
    canActivate: [accessGuard],
    data: { view: "verify", ...student },
  },
  {
    path: "cert/:code",
    loadComponent: portal,
    canActivate: [accessGuard],
    data: { view: "verify", ...student },
  },
  {
    path: "users/panel/comentarios",
    loadComponent: portal,
    canActivate: [accessGuard],
    data: { view: "comments", ...student },
  },
  {
    path: "admin/usuarios/nuevo",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "users", editor: "user", ...adminOnly },
  },
  {
    path: "admin/usuarios/:id/editar",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "users", editor: "user", ...adminOnly },
  },
  ...["admin/user", "admin/usuarios", "admin/profesores"].map((path) => ({
    path,
    loadComponent: admin,
    canActivate: [accessGuard],
    data: {
      view: "users",
      teachers: path.endsWith("profesores"),
      ...adminOnly,
    },
  })),
  {
    path: "admin/pagos/nuevo",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "payments", editor: "payment", ...adminOnly },
  },
  {
    path: "admin/pagos/:id/editar",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "payments", editor: "payment", ...adminOnly },
  },
  {
    path: "admin/pagos",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "payments", ...adminOnly },
  },
  {
    path: "admin/resenas",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "comments", ...adminOnly },
  },
  {
    path: "admin/sesiones",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "sessions", ...adminOnly },
  },
  {
    path: "admin/vista-previa/:profile/:id",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "previewAccount", ...adminOnly },
  },
  {
    path: "admin/vista-previa",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "preview", ...adminOnly },
  },
  {
    path: "admin/matriculas",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "enrollments", ...adminOnly },
  },
  {
    path: "admin/certificados",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "certificates", ...adminOnly },
  },
  {
    path: "admin/auditoria",
    loadComponent: admin,
    canActivate: [accessGuard],
    data: { view: "audit", ...adminOnly },
  },
  {
    path: "admin/cursos/nuevo",
    loadComponent: management,
    canActivate: [accessGuard],
    data: { view: "list", editor: "course", ...adminOnly },
  },
  {
    path: "admin/cursos/:id/editar",
    loadComponent: management,
    canActivate: [accessGuard],
    data: { view: "list", editor: "course", ...adminOnly },
  },
  ...["admin/cursos", "gestion/cursos", "profesor/cursos"].map((path) => ({
    path,
    loadComponent: management,
    canActivate: [accessGuard],
    data: {
      view: "list",
      ...(path.startsWith("admin")
        ? adminOnly
        : path.startsWith("profesor")
          ? { roles: ["DOCENTE"] }
          : manager),
    },
  })),
  {
    path: "gestion/cursos/:id/seguimiento",
    loadComponent: management,
    canActivate: [accessGuard],
    data: { view: "report", ...manager },
  },
  {
    path: "gestion/cursos/:id",
    loadComponent: management,
    canActivate: [accessGuard],
    data: { view: "manage", ...manager },
  },
  {
    path: "gestion/contenidos/:id/preguntas",
    loadComponent: management,
    canActivate: [accessGuard],
    data: { view: "questions", ...manager },
  },
  {
    path: "gestion/contenidos/:id/editar",
    loadComponent: management,
    canActivate: [accessGuard],
    data: { view: "contentEditor", ...manager },
  },
  {
    path: "gestion/modulos/:moduleId/contenidos/nuevo",
    loadComponent: management,
    canActivate: [accessGuard],
    data: { view: "contentEditor", ...manager },
  },
  {
    path: "aula/cursos/:id",
    loadComponent: course,
    canActivate: [accessGuard],
    data: { view: "classroom" },
  },
  {
    path: "contenidos/:id",
    loadComponent: learning,
    canActivate: [accessGuard],
  },
  { path: "vista-previa/:id", loadComponent: learning },
  {
    path: "evaluaciones/intentos/:id",
    loadComponent: learning,
    canActivate: [accessGuard],
    data: { view: "result" },
  },
  {
    path: "acceso-denegado",
    loadComponent: () => import("./pages/error").then((m) => m.ErrorComponent),
    data: { status: 403 },
  },
  {
    path: "**",
    loadComponent: () => import("./pages/error").then((m) => m.ErrorComponent),
  },
];
