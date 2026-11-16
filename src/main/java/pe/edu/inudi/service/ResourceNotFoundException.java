package pe.edu.inudi.service;

/** Ausencia de un recurso; MVC conserva su manejo y REST responde con 404. */
public class ResourceNotFoundException extends BusinessException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
