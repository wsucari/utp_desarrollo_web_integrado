package pe.edu.inudi.web.form;

import jakarta.validation.constraints.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.inudi.domain.*;

@Getter
@Setter
public class ContentForm {
  @NotBlank
  @Size(max = 180)
  private String title;

  @NotNull private ContentType type = ContentType.TEXTO;

  @Size(max = 12000)
  private String body = "";

  @Size(max = 1000)
  private String resourceUrl = "";

  private boolean preview;

  @Min(1)
  @Max(999)
  private int position = 1;

  @Min(0)
  @Max(20)
  private int maxAttempts = 3;

  @Min(0)
  @Max(100)
  private int passPercent = 70;

  private boolean examActive = true;
}
