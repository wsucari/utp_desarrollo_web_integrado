package pe.edu.inudi.web.form;

import jakarta.validation.constraints.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.inudi.domain.*;

@Getter
@Setter
public class AccountForm {
  @NotBlank
  @Size(min = 2, max = 120)
  private String fullName;

  @NotBlank
  @Email
  @Size(max = 180)
  private String email;

  @Size(max = 100)
  private String password = "";

  @NotNull private Role role = Role.ESTUDIANTE;
  private boolean active = true;

  @Size(max = 180)
  private String professionalTitle = "";

  @Size(max = 30)
  private String phone = "";
}
