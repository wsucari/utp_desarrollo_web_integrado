import { TestBed } from "@angular/core/testing";
import { provideHttpClient, withInterceptors } from "@angular/common/http";
import {
  provideHttpClientTesting,
  HttpTestingController,
} from "@angular/common/http/testing";
import {
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  provideRouter,
  UrlTree,
} from "@angular/router";
import {
  Api,
  Auth,
  CsrfStore,
  accessGuard,
  csrfInterceptor,
  failure,
} from "./api";
import { Account } from "./models";

describe("Comunicación y sesión de Angular", () => {
  let api: Api;
  let http: HttpTestingController;
  let auth: Auth;
  let csrf: CsrfStore;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([csrfInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    api = TestBed.inject(Api);
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(Auth);
    csrf = TestBed.inject(CsrfStore);
  });
  afterEach(() => http.verify());
  it("envía filtros como parámetros y admite una lista vacía", async () => {
    const result = api.get<{ courses: unknown[] }>("/cursos", {
      search: "investigación & tesis",
    });
    const request = http.expectOne((r) => r.url === "/api/cursos");
    expect(request.request.params.get("search")).toBe("investigación & tesis");
    expect(request.request.headers.has("X-CSRF-TOKEN")).toBe(false);
    request.flush({ courses: [] });
    expect((await result).courses).toEqual([]);
  });
  it("protege escrituras con el token CSRF", async () => {
    csrf.token = "token-de-sesion";
    const result = api.send("POST", "/carrito/1");
    const request = http.expectOne("/api/carrito/1");
    expect(request.request.headers.get("X-CSRF-TOKEN")).toBe("token-de-sesion");
    request.flush(null, { status: 204, statusText: "No Content" });
    await result;
  });
  it("no añade CSRF a las descargas de lectura", async () => {
    csrf.token = "privado";
    const result = api.get("/mis-cursos");
    const request = http.expectOne("/api/mis-cursos");
    expect(request.request.headers.has("X-CSRF-TOKEN")).toBe(false);
    request.flush([]);
    await result;
  });
  it("actualiza el token después del inicio de sesión", async () => {
    csrf.token = "anterior";
    const result = auth.login("estudiante@inudi.local", "Inudi2026!Demo");
    const login = http.expectOne("/api/auth/login");
    expect(login.request.headers.get("X-CSRF-TOKEN")).toBe("anterior");
    login.flush({});
    await Promise.resolve();
    http
      .expectOne("/api/auth/csrf")
      .flush({ token: "nuevo", headerName: "X-CSRF-TOKEN" });
    http
      .expectOne("/api/auth/session")
      .flush({ user: { id: 1, role: "ESTUDIANTE" }, cartCount: 2, demo: true });
    await result;
    expect(csrf.token).toBe("nuevo");
    expect(auth.user()?.role).toBe("ESTUDIANTE");
    expect(auth.cartCount()).toBe(2);
  });
  it("conserva los mensajes y campos de validación devueltos por el servidor", async () => {
    const result = api
      .send("POST", "/admin/cursos", {})
      .catch((e) => failure(e));
    http
      .expectOne("/api/admin/cursos")
      .flush(
        {
          message: "Revisa los campos.",
          errors: { title: "El título es obligatorio." },
        },
        { status: 400, statusText: "Bad Request" },
      );
    expect(await result).toMatchObject({
      status: 400,
      errors: { title: "El título es obligatorio." },
    });
  });
  it("ofrece un mensaje recuperable ante pérdida de conexión", async () => {
    const result = api.get("/cursos").catch((e) => failure(e));
    http.expectOne("/api/cursos").error(new ProgressEvent("error"));
    expect(await result).toMatchObject({
      status: 0,
      message: expect.stringContaining("conexión"),
    });
  });
  it("redirige una navegación protegida cuando no existe sesión", async () => {
    vi.spyOn(auth, "initialize").mockResolvedValue();
    const result = await TestBed.runInInjectionContext(() =>
      accessGuard(
        { data: {} } as ActivatedRouteSnapshot,
        { url: "/mis-cursos" } as RouterStateSnapshot,
      ),
    );
    expect((result as UrlTree).toString()).toContain("/login?returnUrl=");
  });
  it("impide la navegación de un estudiante al panel administrativo", async () => {
    vi.spyOn(auth, "initialize").mockResolvedValue();
    auth.user.set({ id: 1, role: "ESTUDIANTE" } as Account);
    const result = await TestBed.runInInjectionContext(() =>
      accessGuard(
        { data: { roles: ["ADMIN"] } } as unknown as ActivatedRouteSnapshot,
        { url: "/admin/user" } as RouterStateSnapshot,
      ),
    );
    expect((result as UrlTree).toString()).toBe("/acceso-denegado");
  });
  it("permite al docente abrir sus rutas asignadas", async () => {
    vi.spyOn(auth, "initialize").mockResolvedValue();
    auth.user.set({ id: 2, role: "DOCENTE" } as Account);
    const result = await TestBed.runInInjectionContext(() =>
      accessGuard(
        {
          data: { roles: ["ADMIN", "DOCENTE"] },
        } as unknown as ActivatedRouteSnapshot,
        { url: "/gestion/cursos" } as RouterStateSnapshot,
      ),
    );
    expect(result).toBe(true);
  });
  it("permite reintentar la inicialización después de una desconexión", async () => {
    const failed = auth.initialize().catch((error) => failure(error));
    http.expectOne("/api/auth/csrf").error(new ProgressEvent("error"));
    http.expectOne("/api/auth/session").flush({user:null,cartCount:0,demo:true});
    expect(await failed).toMatchObject({status:0});
    const retry = auth.initialize();
    http.expectOne("/api/auth/csrf").flush({token:"recuperado",headerName:"X-CSRF-TOKEN"});
    http.expectOne("/api/auth/session").flush({user:null,cartCount:0,demo:true});
    await retry; expect(csrf.token).toBe("recuperado");
  });
});
