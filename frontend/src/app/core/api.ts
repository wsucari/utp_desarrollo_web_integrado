import { Injectable, inject, signal } from "@angular/core";
import {
  HttpClient,
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpParams,
} from "@angular/common/http";
import { Router, CanActivateFn } from "@angular/router";
import { firstValueFrom } from "rxjs";
import { Account, ApiError, Session } from "./models";

@Injectable({ providedIn: "root" })
export class CsrfStore {
  token = "";
  header = "X-CSRF-TOKEN";
}

export const csrfInterceptor: HttpInterceptorFn = (request, next) => {
  const csrf = inject(CsrfStore);
  if (
    request.url.startsWith("/api/") &&
    !["GET", "HEAD", "OPTIONS"].includes(request.method) &&
    csrf.token
  ) {
    request = request.clone({ setHeaders: { [csrf.header]: csrf.token } });
  }
  return next(request);
};

@Injectable({ providedIn: "root" })
export class Api {
  private http = inject(HttpClient);
  get<T>(path: string, params: Record<string, string> = {}): Promise<T> {
    return firstValueFrom(
      this.http.get<T>("/api" + path, {
        params: new HttpParams({ fromObject: params }),
      }),
    );
  }
  send<T = void>(
    method: "POST" | "PUT" | "DELETE",
    path: string,
    body: unknown = {},
  ): Promise<T> {
    return firstValueFrom(
      this.http.request<T>(method, "/api" + path, { body }),
    );
  }
  upload<T>(path: string, file: File, comment = ""): Promise<T> {
    const data = new FormData();
    data.append("file", file);
    data.append("comment", comment);
    return this.send<T>("POST", path, data);
  }
}

@Injectable({ providedIn: "root" })
export class Auth {
  private api = inject(Api);
  private csrf = inject(CsrfStore);
  user = signal<Account | null>(null);
  cartCount = signal(0);
  demo = signal(false);
  private ready?: Promise<void>;
  initialize(): Promise<void> {
    return (this.ready ??= this.refresh().catch((error) => {
      this.ready = undefined;
      throw error;
    }));
  }
  async refresh(): Promise<void> {
    const [csrf, session] = await Promise.all([
      this.api.get<{ token: string; headerName: string }>("/auth/csrf"),
      this.api.get<Session>("/auth/session"),
    ]);
    this.csrf.token = csrf.token;
    this.csrf.header = csrf.headerName;
    this.user.set(session.user);
    this.cartCount.set(session.cartCount);
    this.demo.set(session.demo);
  }
  async login(email: string, password: string): Promise<void> {
    await this.api.send("POST", "/auth/login", { email, password });
    await this.refresh();
  }
  async logout(): Promise<void> {
    await this.api.send("POST", "/auth/logout");
    this.user.set(null);
    await this.refresh();
  }
  home(): string {
    return this.user()?.role === "ADMIN"
      ? "/dashboard"
      : this.user()?.role === "DOCENTE"
        ? "/profesor/dashboard"
        : "/users/panel/inicio";
  }
}

export const accessGuard: CanActivateFn = async (route, state) => {
  const auth = inject(Auth);
  const router = inject(Router);
  try {
    await auth.initialize();
  } catch {
    return router.createUrlTree(["/login"]);
  }
  if (!auth.user())
    return router.createUrlTree(["/login"], {
      queryParams: { returnUrl: state.url },
    });
  const roles = route.data["roles"] as string[] | undefined;
  return !roles || roles.includes(auth.user()!.role)
    ? true
    : router.createUrlTree(["/acceso-denegado"]);
};

export function failure(error: unknown): ApiError {
  if (error instanceof HttpErrorResponse) {
    const payload = error.error;
    return {
      status: error.status,
      message:
        payload?.message ||
        (error.status === 0
          ? "No se pudo conectar con el servidor. Revisa tu conexión y vuelve a intentarlo."
          : "No se pudo completar la operación."),
      errors: payload?.errors || {},
    };
  }
  return {
    status: 0,
    message:
      error instanceof Error
        ? error.message
        : "No se pudo completar la operación.",
    errors: {},
  };
}
