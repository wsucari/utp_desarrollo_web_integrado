import { Component, input, output } from "@angular/core";
@Component({
  selector: "inudi-status",
  standalone: true,
  template: `
    @if (loading()) {
      <p role="status" class="soft-note">Cargando…</p>
    }
    @if (notice()) {
      <div role="status" class="alert success">{{ notice() }}</div>
    }
    @if (error()) {
      <div role="alert" class="alert error">
        {{ error() }}
        <button type="button" class="link-button" (click)="retry.emit()">
          Reintentar
        </button>
      </div>
    }
  `,
})
export class StatusComponent {
  loading = input(false);
  error = input("");
  notice = input("");
  retry = output<void>();
}
