package pe.edu.inudi.web.form;

import jakarta.validation.constraints.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.inudi.domain.*;

@Getter
@Setter
public class QuestionForm {
  @NotNull private QuestionType type = QuestionType.OPCION_MULTIPLE;

  @NotBlank
  @Size(max = 2000)
  private String prompt;

  @Size(max = 3000)
  private String optionsText = "";

  @NotBlank
  @Size(max = 2000)
  private String correctAnswer;

  @Size(max = 2000)
  private String explanation = "";

  @Min(1)
  @Max(100)
  private int points = 1;

  @Min(1)
  @Max(999)
  private int position = 1;
}
