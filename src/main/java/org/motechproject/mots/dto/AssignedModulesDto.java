package org.motechproject.mots.dto;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

public class AssignedModulesDto {

  @Getter
  @Setter
  private String chwId;

  @Getter
  @Setter
  private Set<String> modules;
}
