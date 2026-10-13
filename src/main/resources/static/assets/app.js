"use strict";
const editorModal = document.getElementById("editor-modal");
const editorBody = document.getElementById("editor-body");
const editorStatus = document.getElementById("editor-status");
const editorTitle = document.getElementById("editor-modal-title");
let editorOpener, editorBusy = false, editorRequest;
function modalStatus(message, error = false) {
 editorStatus.textContent = message;
 editorStatus.classList.toggle("is-error", error);
}
function wireEditor() {
 editorBody.querySelectorAll('a.button-ghost, a.back-link').forEach(link => {
  if (link.classList.contains("back-link")) link.remove();
  else { const cancel = document.createElement("button"); cancel.type = "button";
   cancel.className = link.className; cancel.dataset.modalClose = ""; cancel.textContent = "Cancelar";
   link.replaceWith(cancel); }
 });
 editorBody.querySelectorAll('form[method="post"]').forEach(form => {
  form.addEventListener("submit", submitEditor);
 });
 const heading = editorBody.querySelector("h1");
 if (heading) { editorTitle.textContent = heading.textContent; heading.remove(); }
 editorBody.querySelector("input:not([type=hidden]):not([disabled]),select:not([disabled]),textarea,button")?.focus();
}
function useEditorDocument(html, sourceUrl) {
 const doc = new DOMParser().parseFromString(html, "text/html");
 const region = doc.querySelector("[data-editor-content]") || doc.getElementById("main");
 if (!region) throw new Error("No se pudo cargar el formulario.");
 region.querySelectorAll("script").forEach(script => script.remove());
 region.querySelectorAll("form").forEach(form => {
  form.setAttribute("action", new URL(form.getAttribute("action") || sourceUrl, sourceUrl).href);
 });
 editorBody.replaceChildren(...[...region.childNodes].map(node => document.importNode(node, true)));
 wireEditor();
}
async function openEditor(trigger) {
 if (!editorModal || editorBusy) return;
 editorOpener = trigger; editorTitle.textContent = trigger.dataset.modalTitle || trigger.textContent.trim();
 editorBody.replaceChildren(); modalStatus("Cargando formulario…");
 editorModal.showModal(); document.body.classList.add("editor-open");
 try {
  if (trigger.dataset.modalSource) {
   const template = document.querySelector(trigger.dataset.modalSource);
   if (!(template instanceof HTMLTemplateElement)) throw new Error("El formulario no está disponible.");
   editorBody.append(template.content.cloneNode(true)); wireEditor(); modalStatus("");
  } else {
   editorRequest?.abort(); editorRequest = new AbortController();
   const url = new URL(trigger.href, location.href);
   if (url.origin !== location.origin) throw new Error("Destino de formulario no válido.");
   const response = await fetch(url, {credentials:"same-origin",signal:editorRequest.signal, headers:{"X-Requested-With":"XMLHttpRequest"}});
   if (!editorModal.open) return;
   if (new URL(response.url).pathname === "/login") { location.assign(response.url); return; }
   const html = await response.text();
   if (!response.ok) throw new Error(new DOMParser().parseFromString(html,"text/html").querySelector("#main p")?.textContent || "No tienes acceso a este formulario.");
   useEditorDocument(html, response.url); modalStatus("");
  }
 } catch(error) { if (error.name !== "AbortError") modalStatus(error.message || "No se pudo cargar el formulario.", true); }
}
async function submitEditor(event) {
 event.preventDefault();
 const form = event.currentTarget;
 if (editorBusy || !form.reportValidity()) return;
 const buttons = [...editorBody.querySelectorAll("button")];
 const previous = buttons.map(button=>button.disabled);
 editorBusy = true; buttons.forEach(button=>button.disabled=true); modalStatus("Guardando…");
 try {
  const multipart = form.enctype === "multipart/form-data";
  const formData = new FormData(form);
  const options = {method:"POST",credentials:"same-origin",headers:{"X-Requested-With":"XMLHttpRequest"}};
  if (multipart) options.body = formData;
  else { options.headers["Content-Type"]="application/x-www-form-urlencoded"; options.body=new URLSearchParams(formData); }
  const response = await fetch(form.action, options);
  const html = await response.text();
  const doc = new DOMParser().parseFromString(html,"text/html");
  if (response.redirected && new URL(response.url).pathname === "/login") { location.assign(response.url); return; }
  if (response.ok && response.redirected) {
   const notice = doc.querySelector(".alert.success")?.textContent.trim() || "Registro guardado correctamente.";
   sessionStorage.setItem("inudi-modal-notice", notice);
   location.reload(); return;
  }
  if (response.ok && doc.querySelector(".form-errors")) {
   useEditorDocument(html,response.url); modalStatus("Revisa los campos indicados.",true);
  } else {
   const message = doc.querySelector("#main .empty-state p")?.textContent.trim()
      || doc.querySelector("#main p")?.textContent.trim() || "No se pudo guardar. Revisa los datos e inténtalo de nuevo.";
   modalStatus(message,true);
  }
 } catch(error) { modalStatus("No se pudo conectar. Los datos siguen en el formulario.",true); }
 finally { editorBusy=false; buttons.forEach((button,index)=>button.disabled=previous[index]); }
}
function closeEditor() {
 if (editorBusy) return;
 editorRequest?.abort(); editorModal?.close(); document.body.classList.remove("editor-open");
 editorOpener?.focus();
}
document.addEventListener("click", event => {
 const close = event.target.closest("[data-modal-close]");
 if (close) { event.preventDefault(); closeEditor(); return; }
 const trigger = event.target.closest("[data-form-modal],[data-modal-source]");
 if (trigger && !event.ctrlKey && !event.metaKey && !event.shiftKey) { event.preventDefault(); openEditor(trigger); }
});
editorModal?.addEventListener("cancel", event => { event.preventDefault(); closeEditor(); });
editorModal?.addEventListener("click", event => { if (event.target===editorModal) {
 const rect=editorModal.getBoundingClientRect();
 if (event.clientX<rect.left || event.clientX>rect.right || event.clientY<rect.top || event.clientY>rect.bottom) closeEditor();
}});
const savedNotice = sessionStorage.getItem("inudi-modal-notice");
if (savedNotice) {
 sessionStorage.removeItem("inudi-modal-notice");
 const main=document.getElementById("main");
 const alert=document.createElement("div"); alert.className="alert success"; alert.setAttribute("role","status");
 alert.textContent=savedNotice; main?.prepend(alert);
}
document.querySelectorAll(".table-wrap").forEach(region => {
 if (!region.hasAttribute("tabindex")) region.setAttribute("tabindex", "0");
 region.setAttribute("role", "region");
 if (!region.hasAttribute("aria-label")) region.setAttribute("aria-label", "Tabla con desplazamiento horizontal");
});
const navigationButton = document.querySelector("[data-nav-toggle]");
const navigationSidebar = document.getElementById("sidebar");
const navigationBackdrop = document.getElementById("navigation-backdrop");
function closeAccountMenus() {
 document.querySelectorAll(".account-menu [data-toggle-target]").forEach(button => {
  const menu = document.getElementById(button.dataset.toggleTarget);
  menu?.classList.remove("reference-open");
  menu?.classList.add("tw-hidden");
  button.setAttribute("aria-expanded", "false");
 });
}
function setNavigation(open, restoreFocus = false) {
 if (!navigationButton || !navigationSidebar) return;
 navigationSidebar.classList.toggle("reference-open", open);
 navigationSidebar.classList.toggle("tw-hidden", !open);
 navigationBackdrop?.classList.toggle("tw-hidden", !open);
 document.body.classList.toggle("navigation-open", open);
 navigationButton.setAttribute("aria-expanded", String(open));
 navigationButton.setAttribute("aria-label", open ? "Cerrar menú" : "Abrir menú");
 if (!open) closeAccountMenus();
 if (open) navigationSidebar.querySelector("button, a[href]")?.focus();
 else if (restoreFocus) navigationButton.focus();
}
navigationButton?.addEventListener("click", () =>
 setNavigation(navigationButton.getAttribute("aria-expanded") !== "true"));
navigationBackdrop?.addEventListener("click", () => setNavigation(false, true));
window.addEventListener("resize", () => {
 if (window.innerWidth >= 768 && navigationButton?.getAttribute("aria-expanded") === "true")
  setNavigation(false);
});
document.querySelectorAll("[data-toggle-target]").forEach(button => {
 button.addEventListener("click", () => {
  const target = document.getElementById(button.dataset.toggleTarget);
  if (!target) return;
  const open = !target.classList.contains("reference-open");
  if (button.closest(".account-menu")) closeAccountMenus();
  target.classList.toggle("reference-open", open);
  target.classList.toggle("tw-hidden", !open);
  button.setAttribute("aria-expanded", String(open));
 });
});
document.addEventListener("click", event => {
 if (!event.target.closest(".account-menu")) closeAccountMenus();
});
document.addEventListener("keydown", event => {
 if (event.key === "Escape") {
  if (document.querySelector(".account-menu .reference-open")) {
   const opener = document.querySelector('.account-menu button[aria-expanded="true"]');
   closeAccountMenus(); opener?.focus();
  } else if (navigationButton?.getAttribute("aria-expanded") === "true") setNavigation(false, true);
  document.querySelectorAll("[data-toggle-target]").forEach(button => {
   const target = document.getElementById(button.dataset.toggleTarget);
   if (target?.classList.contains("reference-open")) {
    target.classList.remove("reference-open"); target.classList.add("tw-hidden");
    button.setAttribute("aria-expanded", "false");
   }
  });
  document.querySelectorAll("details[open]").forEach(detail => detail.removeAttribute("open"));
 }
 if (event.key === "Tab" && navigationButton?.getAttribute("aria-expanded") === "true") {
  const controls = [navigationButton, ...navigationSidebar.querySelectorAll('a[href],button,input,select,textarea,[tabindex="0"]')]
   .filter(element => element.getClientRects().length && !element.disabled);
  const first = controls[0], last = controls[controls.length - 1];
  if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last?.focus(); }
  else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first?.focus(); }
 }
});
document.querySelectorAll("[data-password-toggle]").forEach(button => {
 button.addEventListener("click", () => {
  const input = document.getElementById(button.dataset.passwordToggle);
  if (!input) return;
  const show = input.type === "password";
  input.type = show ? "text" : "password";
  button.setAttribute("aria-pressed", String(show));
  button.setAttribute("aria-label", show ? "Ocultar contraseña" : "Mostrar contraseña");
 });
});
document.querySelectorAll("[data-confirm]").forEach(form => form.addEventListener("submit", event => {
 if (!window.confirm(form.dataset.confirm)) event.preventDefault();
}));
document.querySelectorAll(".go-back").forEach(button => button.addEventListener("click", () => {
 if (history.length > 1) history.back(); else location.assign("/");
}));
document.querySelectorAll("[data-table-filter]").forEach(input => {
 const table = document.getElementById(input.dataset.tableFilter);
 input.addEventListener("input", () => {
  const search = input.value.toLocaleLowerCase("es");
  table?.querySelectorAll("tbody tr").forEach(row => { row.hidden = !row.textContent.toLocaleLowerCase("es").includes(search); });
 });
});
