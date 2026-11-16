import { Component, input, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { Post } from "../core/models";
@Component({
  selector: "inudi-comments",
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="tw-bg-white tw-rounded-lg tw-shadow tw-p-5 tw-mt-6">
      <label class="search-field"
        ><svg><use href="#i-search" /></svg
        ><input
          [ngModel]="query()"
          (ngModelChange)="query.set($event)"
          placeholder="Buscar comentarios..."
          aria-label="Buscar comentarios"
      /></label>
      <div
        class="table-wrap"
        tabindex="0"
        role="region"
        aria-label="Comentarios"
      >
        <table>
          <thead>
            <tr>
              <th>Usuario</th>
              <th>Curso</th>
              <th>Contenido</th>
              <th>Comentario</th>
              <th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            @for (comment of filtered; track comment.id) {
              <tr>
                <td>{{ comment.author.fullName }}</td>
                <td>{{ comment.content.courseTitle }}</td>
                <td>
                  <a [routerLink]="['/contenidos', comment.content.id]">{{
                    comment.content.title
                  }}</a>
                </td>
                <td>{{ comment.body }}</td>
                <td>{{ comment.createdAt }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      @if (!filtered.length) {
        <p class="muted">No hay comentarios registrados.</p>
      }
    </section>
  `,
})
export class CommentsComponent {
  comments = input<Post[]>([]);
  query = signal("");
  get filtered(): Post[] {
    const q = this.query().toLocaleLowerCase("es");
    return this.comments().filter(
      (c) => !q || JSON.stringify(c).toLocaleLowerCase("es").includes(q),
    );
  }
}
