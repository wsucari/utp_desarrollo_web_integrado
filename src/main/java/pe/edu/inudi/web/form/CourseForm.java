package pe.edu.inudi.web.form;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.inudi.domain.*;

@Getter
@Setter
public class CourseForm {
  @NotBlank
  @Size(max = 180)
  private String title;

  @NotBlank
  @Size(max = 6000)
  private String description;

  @NotBlank
  @Size(max = 2000)
  private String objectives;

  @NotBlank
  @Size(max = 100)
  private String category;

  @NotBlank
  @Size(max = 50)
  private String level;

  @Min(1)
  @Max(2000)
  private int durationHours = 12;

  @NotNull
  @DecimalMin("0.00")
  @DecimalMax("999999.99")
  private BigDecimal price = BigDecimal.ZERO;

  private boolean published;
  private boolean certificateEnabled = true;
  private List<Long> teacherIds = new ArrayList<>();
}
