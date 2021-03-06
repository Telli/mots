package org.motechproject.mots.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.motechproject.mots.domain.BaseTimestampedEntity;
import org.motechproject.mots.domain.District;
import org.motechproject.mots.domain.Facility;
import org.motechproject.mots.domain.Sector;
import org.motechproject.mots.domain.Village;
import org.motechproject.mots.testbuilder.DistrictDataBuilder;
import org.motechproject.mots.testbuilder.FacilityDataBuilder;
import org.motechproject.mots.testbuilder.SectorDataBuilder;
import org.motechproject.mots.testbuilder.VillageDataBuilder;
import org.motechproject.mots.utils.WithMockAdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@WithMockAdminUser
public class VillageRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<Village> {

  @Autowired
  private VillageRepository villageRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private SectorRepository sectorRepository;

  @Autowired
  private DistrictRepository districtRepository;

  private final District district = new DistrictDataBuilder().buildAsNew();

  private final Sector sector = new SectorDataBuilder()
      .withDistrict(district)
      .buildAsNew();

  private final Facility facility = new FacilityDataBuilder()
      .withSector(sector)
      .buildAsNew();

  private final Village village1 = generateInstance();
  private final Village village2 = generateInstance();

  /**
   * Prepare the test environment.
   */
  @Before
  public void setUp() {
    districtRepository.save(district);
    sectorRepository.save(sector);
    facilityRepository.save(facility);
    villageRepository.save(village1);
    villageRepository.save(village2);
  }

  @Override
  protected VillageRepository getRepository() {
    return this.villageRepository;
  }

  @Test
  public void shouldFindVillageByName() {
    // when
    Page<Village> result = villageRepository.search(
        village1.getName(), null, null, null, PageRequest.of(0, 100));

    // then
    assertThat(result.getTotalElements(), is(1L));
    Village foundVillage = result.getContent().get(0);
    assertThat(foundVillage.getName(), is(village1.getName()));
  }

  @Test
  public void shouldFindVillageByDistrict() {
    // when
    Page<Village> result = villageRepository.search(
        null, village1.getParentName(), null, null, PageRequest.of(0, 100));

    // then
    assertThat(result.getTotalElements(), is(2L));
    Set<UUID> foundVillages = result.getContent().stream()
        .map(BaseTimestampedEntity::getId)
        .collect(Collectors.toSet());
    assertTrue(foundVillages.contains(village1.getId()));
    assertTrue(foundVillages.contains(village2.getId()));
  }

  @Override
  protected Village generateInstance() {
    return getVillageDataBuilder().buildAsNew();
  }

  private VillageDataBuilder getVillageDataBuilder() {
    return new VillageDataBuilder().withFacility(facility);
  }
}
