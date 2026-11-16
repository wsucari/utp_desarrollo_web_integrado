import { inject, provideAppInitializer } from "@angular/core";
import { bootstrapApplication } from "@angular/platform-browser";
import { provideHttpClient, withInterceptors } from "@angular/common/http";
import { provideRouter, withInMemoryScrolling } from "@angular/router";
import { AppComponent } from "./app/app";
import { Auth, csrfInterceptor } from "./app/core/api";
import { routes } from "./app/routes";
bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withInterceptors([csrfInterceptor])),
    provideRouter(
      routes,
      withInMemoryScrolling({
        scrollPositionRestoration: "enabled",
        anchorScrolling: "enabled",
      }),
    ),
    provideAppInitializer(() =>
      inject(Auth)
        .initialize()
        .catch(() => undefined),
    ),
  ],
}).catch((error) => console.error("No se pudo iniciar la interfaz.", error));
