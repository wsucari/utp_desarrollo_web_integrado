import { TestBed, ComponentFixture } from "@angular/core/testing";
import { HttpErrorResponse } from "@angular/common/http";
import { EditorComponent } from "./editor";
describe("Formularios modales", () => {
  let fixture: ComponentFixture<EditorComponent>;
  let editor: EditorComponent;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditorComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(EditorComponent);
    fixture.detectChanges();
    editor = fixture.componentInstance;
    Object.defineProperty(editor.dialog.nativeElement, "showModal", {
      value: () => editor.dialog.nativeElement.setAttribute("open", ""),
      configurable: true,
    });
    Object.defineProperty(editor.dialog.nativeElement, "close", {
      value: () => editor.dialog.nativeElement.removeAttribute("open"),
      configurable: true,
    });
  });
  it("mantiene los datos y el modal abierto ante validación del servidor", async () => {
    editor.open({
      title: "Crear curso",
      fields: [{ name: "title", label: "Título", required: true }],
      initial: { title: "Título ingresado" },
      save: async () => {
        throw new HttpErrorResponse({
          status: 422,
          error: { message: "El curso ya existe." },
        });
      },
    });
    await editor.submit();
    expect(editor.form.getRawValue()["title"]).toBe("Título ingresado");
    expect(editor.error()).toBe("El curso ya existe.");
    expect(editor.dialog.nativeElement.open).toBe(true);
  });
  it("rechaza una nota superior a veinte sin enviarla", async () => {
    const save = vi.fn();
    editor.open({
      title: "Calificar actividad",
      fields: [
        {
          name: "grade",
          label: "Nota sobre 20",
          type: "number",
          min: 0,
          max: 20,
          required: true,
        },
      ],
      initial: { grade: 21 },
      save,
    });
    await editor.submit();
    expect(save).not.toHaveBeenCalled();
    expect(editor.form.invalid).toBe(true);
  });
  it("conserva las identidades bloqueadas al editar un pago", async () => {
    const save = vi.fn().mockResolvedValue({});
    editor.open({
      title: "Editar pago",
      fields: [
        {
          name: "studentId",
          label: "Estudiante",
          type: "select",
          disabled: true,
        },
        { name: "courseId", label: "Curso", type: "select", disabled: true },
        { name: "reference", label: "Referencia", required: true },
      ],
      initial: { studentId: 4, courseId: 7, reference: "Sustento válido" },
      save,
    });
    await editor.submit();
    expect(save).toHaveBeenCalledWith({
      studentId: 4,
      courseId: 7,
      reference: "Sustento válido",
    });
    expect(editor.dialog.nativeElement.open).toBe(false);
  });
  it("conserva errores de campo y permite corregirlos", async () => {
    editor.open({
      title: "Usuario",
      fields: [
        { name: "email", label: "Correo", type: "email", required: true },
      ],
      initial: { email: "correo@inudi.local" },
      save: async () => {
        throw new HttpErrorResponse({
          status: 400,
          error: {
            message: "Revisa el correo.",
            errors: { email: "El correo ya está registrado." },
          },
        });
      },
    });
    await editor.submit();
    expect(editor.fieldError("email")).toBe("El correo ya está registrado.");
    editor.form.controls["email"].setValue("nuevo@inudi.local");
    expect(editor.form.valid).toBe(true);
  });
  it("vincula cada reapertura con el formulario nuevo aunque se repitan los campos", async () => {
    const fields = [{name:"title",label:"Título",required:true}];
    const save = vi.fn().mockResolvedValue({});
    editor.open({title:"Nuevo contenido",fields,initial:{title:"Primero"},save});
    await editor.submit();
    editor.open({title:"Nuevo contenido",fields,initial:{title:""},save});
    const input = fixture.nativeElement.querySelector("#editor-title") as HTMLInputElement;
    input.value="Segundo contenido"; input.dispatchEvent(new Event("input",{bubbles:true}));
    await editor.submit();
    expect(save).toHaveBeenLastCalledWith({title:"Segundo contenido"});
    expect(editor.dialog.nativeElement.open).toBe(false);
  });
});
