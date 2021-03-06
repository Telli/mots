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
import org.motechproject.mots.domain.CommunityHealthWorker;
import org.motechproject.mots.domain.District;
import org.motechproject.mots.domain.Facility;
import org.motechproject.mots.domain.Sector;
import org.motechproject.mots.domain.Village;
import org.motechproject.mots.testbuilder.CommunityHealthWorkerDataBuilder;
import org.motechproject.mots.testbuilder.DistrictDataBuilder;
import org.motechproject.mots.testbuilder.FacilityDataBuilder;
import org.motechproject.mots.testbuilder.SectorDataBuilder;
import org.motechproject.mots.testbuilder.VillageDataBuilder;
import org.motechproject.mots.utils.WithMockAdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@WithMockAdminUser
public class CommunityHealthWorkerRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<CommunityHealthWorker> {

  @Autowired
  private VillageRepository villageRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private SectorRepository sectorRepository;

  @Autowired
  private DistrictRepository districtRepository;

  @Autowired
  private CommunityHealthWorkerRepository repository;

  private final District district = new DistrictDataBuilder().buildAsNew();

  private final Sector sector = new SectorDataBuilder()
      .withDistrict(district)
      .buildAsNew();

  private final Facility facility = new FacilityDataBuilder()
      .withSector(sector)
      .buildAsNew();

  private final Village village = new VillageDataBuilder()
      .withFacility(facility)
      .buildAsNew();

  private final CommunityHealthWorker chw1 = generateInstance();
  private final CommunityHealthWorker chw2 = generateInstance();

  @Override
  protected CommunityHealthWorkerRepository getRepository() {
    return this.repository;
  }

  /**
   * Prepare the test environment.
   */
  @Before
  public void setUp() {
    districtRepository.save(district);
    sectorRepository.save(sector);
    facilityRepository.save(facility);
    villageRepository.save(village);

    repository.save(chw1);
    repository.save(chw2);
  }

  @Test
  public void shouldFindChw() {
    // when
    Page<CommunityHealthWorker> result = repository.searchCommunityHealthWorkers(chw1.getChwId(),
        chw1.getChwName(), chw1.getPhoneNumber(),
        village.getName(),
        facility.getName(), sector.getName(),
        district.getName(), null, false, PageRequest.of(0, 100));

    // then
    assertThat(result.getTotalElements(), is(1L));
    CommunityHealthWorker foundWorker = result.getContent().get(0);
    assertThat(foundWorker.getId(), is(chw1.getId()));
  }

  @Test
  public void shouldFindChwByChwName() {
    // when
    Page<CommunityHealthWorker> result = repository.searchCommunityHealthWorkers(null,
        chw2.getChwName(), null, null,
        null, null, null, null, null,  PageRequest.of(0, 100));

    // then
    assertThat(result.getTotalElements(), is(1L));
    CommunityHealthWorker foundWorker = result.getContent().get(0);
    assertThat(foundWorker.getChwName(), is(chw2.getChwName()));
  }

  @Test
  public void shouldFindChwByDistrict() {
    // when
    Page<CommunityHealthWorker> result = repository.searchCommunityHealthWorkers(null,
        null, null, null,
        null, null,  district.getName(), null, false, PageRequest.of(0, 100));

    // then
    assertThat(result.getTotalElements(), is(2L));
    Set<UUID> foundChws = result.getContent().stream()
        .map(BaseTimestampedEntity::getId)
        .collect(Collectors.toSet());
    assertTrue(foundChws.contains(chw1.getId()));
    assertTrue(foundChws.contains(chw2.getId()));
  }

  /**
   * Generate a Community Health Worker.
   * @return CHW
   */
  @Override
  protected CommunityHealthWorker generateInstance() {
    return getChwDataBuilder()
        .buildAsNew();
  }

  private CommunityHealthWorkerDataBuilder getChwDataBuilder() {
    return new CommunityHealthWorkerDataBuilder()
        .withDistrict(district)
        .withSector(sector)
        .withFacility(facility)
        .withVillage(village);
  }
}
