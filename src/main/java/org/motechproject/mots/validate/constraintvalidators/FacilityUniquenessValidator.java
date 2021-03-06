package org.motechproject.mots.validate.constraintvalidators;

import java.util.UUID;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.motechproject.mots.constants.ValidationMessageConstants;
import org.motechproject.mots.domain.Facility;
import org.motechproject.mots.domain.Sector;
import org.motechproject.mots.dto.FacilityCreationDto;
import org.motechproject.mots.repository.FacilityRepository;
import org.motechproject.mots.repository.SectorRepository;
import org.motechproject.mots.validate.ValidationUtils;
import org.motechproject.mots.validate.annotations.FacilityUniqueness;
import org.springframework.beans.factory.annotation.Autowired;

public class FacilityUniquenessValidator implements
    ConstraintValidator<FacilityUniqueness, FacilityCreationDto> {

  private static final String NAME = "name";

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private SectorRepository sectorRepository;

  @Override
  public boolean isValid(FacilityCreationDto facilityCreationDto,
      ConstraintValidatorContext context) {

    if (StringUtils.isNotEmpty(facilityCreationDto.getSectorId())
        && ValidationUtils.isValidUuidString(facilityCreationDto.getSectorId())
        && StringUtils.isNotEmpty(facilityCreationDto.getName())) {

      String name = facilityCreationDto.getName();
      UUID sectorId = UUID.fromString(facilityCreationDto.getSectorId());
      Sector sector = sectorRepository.getOne(sectorId);

      Facility existing = facilityRepository.findByNameAndSector(name, sector);

      if (existing != null // when edit facility allows change
          && !existing.getId().toString().equals(facilityCreationDto.getId())) {
        String message = String.format(ValidationMessageConstants.NOT_UNIQUE_FACILITY,
            existing.getName());

        context.disableDefaultConstraintViolation();
        ValidationUtils.addDefaultViolationMessageToInnerField(context, NAME, message);
        return false;
      }
    }

    return true;
  }

  @Override
  public void initialize(FacilityUniqueness parameters) {
    // we don't need any passed parameters
  }
}
