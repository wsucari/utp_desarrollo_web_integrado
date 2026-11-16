import { Component, input } from "@angular/core";
import { RouterLink } from "@angular/router";
import { SyllabusRow } from "../core/models";
@Component({
  selector: "inudi-syllabus",
  standalone: true,
  imports: [RouterLink],
  template: `
    @for (row of rows(); track row.module.id) {
      <div class="syllabus-module">
        <h3>{{ row.module.position }}. {{ row.module.title }}</h3>
        @for (lesson of row.contents; track lesson.id) {
          <div class="syllabus-item">
            <span
              class="lesson-icon"
              [class.done]="completed().includes(lesson.id)"
              >{{
                completed().includes(lesson.id)
                  ? "✓"
                  : lesson.type === "VIDEO"
                    ? "▷"
                    : lesson.type === "EVALUACION"
                      ? "✓"
                      : lesson.type === "FORO"
                        ? "◇"
                        : "▤"
              }}</span
            >
            <div>
              <strong>{{ lesson.title }}</strong
              ><small>{{ lesson.type }}</small>
            </div>
            @if (access()) {
              <a class="text-link" [routerLink]="['/contenidos', lesson.id]"
                >Abrir →</a
              >
            } @else if (lesson.preview) {
              <a class="text-link" [routerLink]="['/vista-previa', lesson.id]"
                >Vista previa →</a
              >
            } @else {
              <span class="muted">Con matrícula</span>
            }
          </div>
        }
      </div>
    }
    @if (!rows().length) {
      <p class="muted">El programa aún no tiene contenidos publicados.</p>
    }
  `,
})
export class SyllabusComponent {
  rows = input<SyllabusRow[]>([]);
  access = input(false);
  completed = input<number[]>([]);
}
