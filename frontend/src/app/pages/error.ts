import { Component, inject } from "@angular/core";
import { ActivatedRoute, RouterLink } from "@angular/router";
@Component({
  standalone: true,
  imports: [RouterLink],
  template: `<section class="panel narrow">
    <span class="eyebrow">{{ denied ? "403" : "404" }}</span>
    <h1>{{ denied ? "Acceso restringido" : "Página no encontrada" }}</h1>
    <p>
      {{
        denied
          ? "Tu perfil no tiene permiso para abrir esta sección."
          : "La página solicitada no está disponible."
      }}
    </p>
    <a routerLink="/" class="button">Volver al catálogo</a>
  </section>`,
})
export class ErrorComponent {
  denied = inject(ActivatedRoute).snapshot.data["status"] === 403;
}
