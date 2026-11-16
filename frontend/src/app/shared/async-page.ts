import { Directive, inject, signal } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { Api, Auth, failure } from "../core/api";
import { Flash } from "../core/flash";
import { combineLatest } from "rxjs";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";

@Directive()
export abstract class AsyncPage {
  api = inject(Api);
  auth = inject(Auth);
  route = inject(ActivatedRoute);
  router = inject(Router);
  flash = inject(Flash);
  loading = signal(false);
  error = signal("");
  notice = signal("");
  saving = signal(false);
  private loadVersion = 0;
  watch(refresh: () => Promise<void>): void {
    combineLatest([this.route.paramMap, this.route.queryParamMap])
      .pipe(takeUntilDestroyed())
      .subscribe(() => void refresh());
  }
  async load(work: () => Promise<void>): Promise<void> {
    const version = ++this.loadVersion;
    this.loading.set(true);
    this.error.set("");
    try {
      await work();
    } catch (error) {
      if (version === this.loadVersion) this.error.set(failure(error).message);
      if (failure(error).status === 401) {
        this.auth.user.set(null);
        await this.router.navigate(["/login"], {
          queryParams: { returnUrl: this.router.url },
        });
      }
    } finally {
      if (version === this.loadVersion) this.loading.set(false);
    }
  }
  async action(
    work: () => Promise<unknown>,
    refresh: () => Promise<void>,
    message = "Cambios guardados.",
  ): Promise<void> {
    if (this.saving()) return;
    this.saving.set(true);
    this.error.set("");
    const previousUrl = this.router.url;
    try {
      await work();
      await refresh();
      if (this.router.url !== previousUrl) this.flash.message.set(message);
      else this.notice.set(message);
    } catch (error) {
      this.error.set(failure(error).message);
    } finally {
      this.saving.set(false);
    }
  }
  includes(row: unknown, query: string): boolean {
    return (
      !query ||
      JSON.stringify(row)
        .toLocaleLowerCase("es")
        .includes(query.toLocaleLowerCase("es"))
    );
  }
  date(value?: string | null): string {
    return value?.slice(0, 10) || "—";
  }
}
