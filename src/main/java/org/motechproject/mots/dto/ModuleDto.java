package org.motechproject.mots.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.motechproject.mots.constants.ValidationMessageConstants;
import org.motechproject.mots.domain.enums.Status;
import org.motechproject.mots.validate.annotations.Uuid;

public class ModuleDto extends IvrObjectDto {

  @Getter
  @Setter
  @Uuid
  private String id;

  @Getter
  @Setter
  private String treeId;

  @NotBlank(message = ValidationMessageConstants.EMPTY_MODULE_NAME)
  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String description;

  @Getter
  @Setter
  private String ivrGroup;

  @Getter
  @Setter
  private Status status;

  @Getter
  @Setter
  private String startModuleQuestionIvrId;

  @Getter
  @Setter
  private String type;

  @Valid
  @Getter
  @Setter
  private List<UnitDto> children;
}
