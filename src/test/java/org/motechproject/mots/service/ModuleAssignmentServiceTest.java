package org.motechproject.mots.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.motechproject.mots.domain.AssignedModules;
import org.motechproject.mots.domain.CommunityHealthWorker;
import org.motechproject.mots.domain.District;
import org.motechproject.mots.domain.DistrictAssignmentLog;
import org.motechproject.mots.domain.Module;
import org.motechproject.mots.domain.ModuleProgress;
import org.motechproject.mots.domain.Sector;
import org.motechproject.mots.domain.enums.ProgressStatus;
import org.motechproject.mots.domain.security.User;
import org.motechproject.mots.dto.DistrictAssignmentDto;
import org.motechproject.mots.dto.ModuleAssignmentDto;
import org.motechproject.mots.exception.EntityNotFoundException;
import org.motechproject.mots.exception.IvrException;
import org.motechproject.mots.exception.ModuleAssignmentException;
import org.motechproject.mots.exception.MotsException;
import org.motechproject.mots.repository.AssignedModulesRepository;
import org.motechproject.mots.repository.CommunityHealthWorkerRepository;
import org.motechproject.mots.repository.DistrictAssignmentLogRepository;
import org.motechproject.mots.repository.DistrictRepository;
import org.motechproject.mots.repository.ModuleRepository;
import org.motechproject.mots.repository.SectorRepository;
import org.motechproject.mots.testbuilder.AssignedModulesDataBuilder;
import org.motechproject.mots.testbuilder.CommunityHealthWorkerDataBuilder;
import org.motechproject.mots.testbuilder.DistrictAssignmentDtoDataBuilder;
import org.motechproject.mots.testbuilder.DistrictDataBuilder;
import org.motechproject.mots.testbuilder.ModuleDataBuilder;
import org.motechproject.mots.testbuilder.ModuleProgressDataBuilder;
import org.motechproject.mots.testbuilder.SectorDataBuilder;
import org.motechproject.mots.testbuilder.UserDataBuilder;
import org.motechproject.mots.utils.AuthenticationHelper;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class ModuleAssignmentServiceTest {

  @Mock
  private AssignedModulesRepository assignedModulesRepository;

  @Mock
  private ModuleProgressService moduleProgressService;

  @Mock
  private CommunityHealthWorkerRepository communityHealthWorkerRepository;

  @Mock
  private ModuleRepository moduleRepository;

  @Mock
  private DistrictRepository districtRepository;

  @Mock
  private SectorRepository sectorRepository;

  @Mock
  private DistrictAssignmentLogRepository districtAssignmentLogRepository;

  @Mock
  private IvrService ivrService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  @SuppressWarnings("unused")
  private EntityManager entityManager;

  @InjectMocks
  private ModuleAssignmentService moduleAssignmentService;

  private static final String IVR_GROUP = "ivrGroup";

  private static final CommunityHealthWorker CHW = new CommunityHealthWorkerDataBuilder()
      .withIvrId("ivrId")
      .build();

  private static final Module MODULE_1 = new ModuleDataBuilder()
      .withIvrGroup(IVR_GROUP)
      .build();

  private static final Module MODULE_2 = new ModuleDataBuilder().build();

  private static final Module MODULE_3 = new ModuleDataBuilder()
      .withIvrGroup(IVR_GROUP)
      .build();

  private static final District DISTRICT = new DistrictDataBuilder().build();

  private static final Sector SECTOR = new SectorDataBuilder().build();

  private AssignedModules existingAssignedModules;

  private AssignedModules newAssignedModules;

  private DistrictAssignmentDto districtAssignmentDto;

  private User user;

  /**
   * Prepare the test environment.
   */
  @Before
  public void setUp() {
    user = new UserDataBuilder().build();
    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    existingAssignedModules = new AssignedModulesDataBuilder()
        .withChw(CHW)
        .withModule(MODULE_1)
        .withModule(MODULE_2)
        .build();

    newAssignedModules = new AssignedModulesDataBuilder()
        .withChw(CHW)
        .withModule(MODULE_2)
        .withModule(MODULE_3)
        .build();

    districtAssignmentDto = new DistrictAssignmentDtoDataBuilder()
        .withModule(MODULE_2.getId().toString())
        .withModule(MODULE_3.getId().toString())
        .withDistrictId(DISTRICT.getId().toString())
        .build();

    when(assignedModulesRepository.findByHealthWorkerId(eq(CHW.getId())))
        .thenReturn(Optional.of(existingAssignedModules));
    when(districtRepository.getOne(eq(DISTRICT.getId())))
        .thenReturn(DISTRICT);
    when(sectorRepository.getOne(eq(SECTOR.getId())))
        .thenReturn(SECTOR);
    mockModuleInModuleRepository(MODULE_2);
    mockModuleInModuleRepository(MODULE_3);
  }

  @Test(expected = EntityNotFoundException.class)
  public void shouldThrowExceptionIfChwDoesNotExist() {
    when(assignedModulesRepository.findByHealthWorkerId(any()))
        .thenReturn(Optional.empty());
    moduleAssignmentService.getAssignedModules(UUID.randomUUID());
  }

  @Test
  public void shouldReturnAssignedModules() {
    AssignedModules actual = moduleAssignmentService.getAssignedModules(CHW.getId());
    assertEquals(existingAssignedModules, actual);
  }

  @Test
  public void shouldAssignModules() throws IvrException {
    when(moduleProgressService.getModuleProgress(any(), any()))
        .thenReturn(getModuleProgress(ProgressStatus.NOT_STARTED));

    moduleAssignmentService.assignModules(toDto(newAssignedModules));

    ArgumentCaptor<AssignedModules> assignedModulesCaptor =
        ArgumentCaptor.forClass(AssignedModules.class);
    verify(assignedModulesRepository).save(assignedModulesCaptor.capture());
    assertEquals(newAssignedModules.getModules(), assignedModulesCaptor.getValue().getModules());

    verify(ivrService)
        .addSubscriberToGroups(eq(CHW.getIvrId()), eq(Collections.singletonList(IVR_GROUP)));
    verify(ivrService)
        .removeSubscriberFromGroups(eq(CHW.getIvrId()), eq(Collections.singletonList(IVR_GROUP)));
    verify(moduleProgressService)
        .removeModuleProgresses(any(), eq(Collections.singleton(MODULE_1)));
    verify(moduleProgressService)
        .createModuleProgresses(any(), eq(Collections.singleton(MODULE_3)));
  }

  @Test(expected = MotsException.class)
  public void shouldThrowWhenTryToUnassignModuleInProgress() {
    when(moduleProgressService.getModuleProgress(any(), any()))
        .thenReturn(getModuleProgress(ProgressStatus.IN_PROGRESS));

    moduleAssignmentService.assignModules(toDto(newAssignedModules));
  }

  @Test(expected = MotsException.class)
  public void shouldThrowWhenTryToUnassignCompletedModule() {
    when(moduleProgressService.getModuleProgress(any(), any()))
        .thenReturn(getModuleProgress(ProgressStatus.COMPLETED));

    moduleAssignmentService.assignModules(toDto(newAssignedModules));
  }

  @Test(expected = ModuleAssignmentException.class)
  public void assignModulesShouldThrowIfIvrIdIsNotSet() {
    final CommunityHealthWorker chw = new CommunityHealthWorkerDataBuilder()
        .withIvrId(null)
        .build();
    final AssignedModules assignedModules = new AssignedModulesDataBuilder()
        .withChw(chw)
        .build();

    when(assignedModulesRepository.findByHealthWorkerId(eq(chw.getId())))
        .thenReturn(Optional.of(assignedModules));

    moduleAssignmentService.assignModules(toDto(assignedModules));
  }

  @Test(expected = ModuleAssignmentException.class)
  public void assignModulesShouldThrowCustomExceptionIfIvrServiceThrow() throws IvrException {
    when(moduleProgressService.getModuleProgress(any(), any()))
        .thenReturn(getModuleProgress(ProgressStatus.NOT_STARTED));

    doThrow(new IvrException("message")).when(ivrService).addSubscriberToGroups(any(), any());

    moduleAssignmentService.assignModules(toDto(newAssignedModules));
  }

  @Test
  public void shouldAssignModulesToDistrict() throws IvrException {
    when(communityHealthWorkerRepository.findByDistrictIdAndSelected(any(),
        any())).thenReturn(Collections.singletonList(CHW));

    moduleAssignmentService.assignModulesToChwsInLocation(districtAssignmentDto);

    ArgumentCaptor<DistrictAssignmentLog> districtAssignmentLogCaptor =
        ArgumentCaptor.forClass(DistrictAssignmentLog.class);
    verify(districtAssignmentLogRepository, times(2)).save(districtAssignmentLogCaptor.capture());
    List<DistrictAssignmentLog> districtAssignmentLogs = districtAssignmentLogCaptor.getAllValues();

    final Set<Module> passedModules = new HashSet<>(Arrays.asList(MODULE_2, MODULE_3));
    assertTrue(districtAssignmentLogs.stream()
        .allMatch(l -> passedModules.contains(l.getModule())));

    for (DistrictAssignmentLog log : districtAssignmentLogs) {
      assertEquals(DISTRICT, log.getDistrict());
      assertEquals(user, log.getOwner());
    }

    ArgumentCaptor<AssignedModules> assignedModulesCaptor =
        ArgumentCaptor.forClass(AssignedModules.class);
    verify(assignedModulesRepository).save(assignedModulesCaptor.capture());
    final Set<Module> allModules = new HashSet<>(Arrays.asList(MODULE_1, MODULE_2, MODULE_3));
    assertEquals(allModules, assignedModulesCaptor.getValue().getModules());

    verify(ivrService)
        .addSubscribersToGroup(eq(IVR_GROUP), eq(Collections.singletonList(CHW.getIvrId())));
    verify(moduleProgressService)
        .createModuleProgresses(any(), eq(Collections.singleton(MODULE_3)));
  }

  @Test
  public void shouldAssignModulesToASector() throws IvrException {
    when(communityHealthWorkerRepository.findBySectorIdAndSelected(
        eq(SECTOR.getId()), any())).thenReturn(Collections.singletonList(CHW));

    districtAssignmentDto.setSectorId(SECTOR.getId().toString());

    moduleAssignmentService.assignModulesToChwsInLocation(districtAssignmentDto);

    ArgumentCaptor<DistrictAssignmentLog> districtAssignmentLogCaptor =
        ArgumentCaptor.forClass(DistrictAssignmentLog.class);
    verify(districtAssignmentLogRepository, times(2)).save(districtAssignmentLogCaptor.capture());
    List<DistrictAssignmentLog> districtAssignmentLogs = districtAssignmentLogCaptor.getAllValues();

    final Set<Module> passedModules = new HashSet<>(Arrays.asList(MODULE_2, MODULE_3));
    assertTrue(districtAssignmentLogs.stream()
        .allMatch(l -> passedModules.contains(l.getModule())));

    for (DistrictAssignmentLog log : districtAssignmentLogs) {
      assertEquals(DISTRICT, log.getDistrict());
      assertEquals(SECTOR, log.getSector());
      assertEquals(user, log.getOwner());
    }

    ArgumentCaptor<AssignedModules> assignedModulesCaptor =
        ArgumentCaptor.forClass(AssignedModules.class);
    verify(assignedModulesRepository).save(assignedModulesCaptor.capture());
    final Set<Module> allModules = new HashSet<>(Arrays.asList(MODULE_1, MODULE_2, MODULE_3));
    assertEquals(allModules, assignedModulesCaptor.getValue().getModules());

    verify(ivrService)
        .addSubscribersToGroup(eq(IVR_GROUP), eq(Collections.singletonList(CHW.getIvrId())));
    verify(moduleProgressService)
        .createModuleProgresses(any(), eq(Collections.singleton(MODULE_3)));
  }

  @Test(expected = ModuleAssignmentException.class)
  public void assignModulesToDistrictShouldThrowIfIvrIdIsNotSet() {
    CommunityHealthWorker chw = new CommunityHealthWorkerDataBuilder()
        .withIvrId(null)
        .build();
    AssignedModules assignedModules = new AssignedModulesDataBuilder()
        .withChw(chw)
        .build();

    when(communityHealthWorkerRepository.findByDistrictIdAndSelected(any(),
        any())).thenReturn(Collections.singletonList(chw));
    when(assignedModulesRepository.findByHealthWorkerId(eq(chw.getId())))
        .thenReturn(Optional.of(assignedModules));

    moduleAssignmentService.assignModulesToChwsInLocation(districtAssignmentDto);
  }

  @Test(expected = ModuleAssignmentException.class)
  public void assignModulesToDistrictShouldThrowCustomExceptionIfIvrServiceThrow()
      throws IvrException {
    when(communityHealthWorkerRepository.findByDistrictIdAndSelected(any(),
        any())).thenReturn(Collections.singletonList(CHW));

    doThrow(new IvrException("message")).when(ivrService).addSubscribersToGroup(any(), any());

    moduleAssignmentService.assignModulesToChwsInLocation(districtAssignmentDto);
  }

  @Test(expected = EntityNotFoundException.class)
  public void shouldThrowIfModulesWithDoesNotExists() {
    UUID moduleId = UUID.randomUUID();
    DistrictAssignmentDto customDistrictAssignmentDto = new DistrictAssignmentDtoDataBuilder()
        .withModule(moduleId.toString())
        .withDistrictId(DISTRICT.getId().toString())
        .build();
    when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

    moduleAssignmentService.assignModulesToChwsInLocation(customDistrictAssignmentDto);
  }

  private ModuleProgress getModuleProgress(ProgressStatus status) {
    return new ModuleProgressDataBuilder()
        .withStatus(status)
        .build();
  }

  private void mockModuleInModuleRepository(Module module) {
    when(moduleRepository.findById(module.getId())).thenReturn(Optional.of(module));
  }

  private ModuleAssignmentDto toDto(AssignedModules assignedModules) {
    ModuleAssignmentDto dto = new ModuleAssignmentDto();
    dto.setChwId(assignedModules.getHealthWorker().getId().toString());
    dto.setModules(assignedModules.getModules().stream()
        .map(module -> module.getId().toString())
        .collect(Collectors.toSet()));
    return dto;
  }
}
