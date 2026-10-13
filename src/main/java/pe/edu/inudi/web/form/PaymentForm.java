package pe.edu.inudi.web.form;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaymentForm {
  @NotNull private Long studentId;
  @NotNull private Long courseId;
  @NotNull @DecimalMin("0.00") @DecimalMax("999999.99") @Digits(integer=6,fraction=2)
  private BigDecimal amount = BigDecimal.ZERO;
  @NotBlank @Size(max=120) private String reference;
  @NotBlank @Pattern(regexp="CONFIRMADO|PENDIENTE|ANULADO") private String status = "CONFIRMADO";
  @NotNull @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
  private LocalDate expiration = LocalDate.now().plusYears(1);
  private Long version;
}
