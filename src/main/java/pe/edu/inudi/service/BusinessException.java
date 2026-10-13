package pe.edu.inudi.service;

public class BusinessException extends RuntimeException {
  public BusinessException(String message) {
    super(message);
  }
}
