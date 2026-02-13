package timdev.timdev;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

import timdev.timdev.entity.Model;
import timdev.timdev.entity.PlanningRepairMaintenance;
import timdev.timdev.entity.Truck;
import timdev.timdev.enums.IntervalType;
import timdev.timdev.enums.PlanningStatus;
import timdev.timdev.enums.TruckSize;
import timdev.timdev.repository.ModelRepository;
import timdev.timdev.repository.PlanningRepairMaintenanceRepository;
import timdev.timdev.repository.TruckRepository;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=YEAR",
    "spring.jpa.properties.hibernate.envers.autoRegisterListeners=false",
    "spring.jpa.properties.hibernate.integration.envers.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@Sql(scripts = "classpath:sql/planning_maintenance_schema.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:sql/planning_maintenance_drop.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class PlanningRepairMaintenanceFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TruckRepository truckRepo;

    @Autowired
    private ModelRepository modelRepo;

    @Autowired
    private PlanningRepairMaintenanceRepository planningRepo;

    @MockBean
    private timdev.timdev.service.AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        planningRepo.deleteAll();
        truckRepo.deleteAll();
        modelRepo.deleteAll();
    }

    @Test
    void create_start_complete_generates_next_plan_for_month_interval() throws Exception {
        Truck truck = createTruck("T-001", "3A-1234");

        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 8);

        mockMvc.perform(post("/maintenance/save")
                .with(csrf())
                .param("truckId", truck.getId().toString())
                .param("planTitle", "Oil Change")
                .param("planTaskText", "Initial service")
                .param("plannedStartDate", start.toString())
                .param("plannedEndDate", end.toString())
                .param("intervalType", IntervalType.MONTH.name())
                .param("intervalValue", "6"))
            .andExpect(status().is3xxRedirection());

        PlanningRepairMaintenance created = planningRepo.findAll().get(0);
        assertThat(created.getStatus()).isEqualTo(PlanningStatus.PLANNED);
        assertThat(created.getPlannedDurationDays()).isEqualTo(7);

        mockMvc.perform(post("/maintenance/start/{id}", created.getPlanningId())
                .with(csrf()))
            .andExpect(status().is3xxRedirection());

        PlanningRepairMaintenance started = planningRepo.findById(created.getPlanningId()).orElseThrow();
        assertThat(started.getStatus()).isEqualTo(PlanningStatus.IN_PROGRESS);
        assertThat(started.getActualStartDate()).isNotNull();
        assertThat(started.getActualStartDate().toLocalDate()).isEqualTo(LocalDate.now());

        mockMvc.perform(post("/maintenance/complete/{id}", created.getPlanningId())
                .with(csrf()))
            .andExpect(status().is3xxRedirection());

        List<PlanningRepairMaintenance> all = planningRepo.findAll();
        assertThat(all).hasSize(2);

        PlanningRepairMaintenance completed = planningRepo.findById(created.getPlanningId()).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo(PlanningStatus.COMPLETED);
        assertThat(completed.getActualEndDate()).isNotNull();
        assertThat(completed.getActualEndDate().toLocalDate()).isEqualTo(LocalDate.now());

        PlanningRepairMaintenance next = all.stream()
            .filter(p -> !p.getPlanningId().equals(created.getPlanningId()))
            .max(Comparator.comparing(PlanningRepairMaintenance::getPlanningId))
            .orElseThrow();

        LocalDate expectedNextDate = completed.getActualEndDate().toLocalDate().plusYears(1);
        assertThat(next.getStatus()).isEqualTo(PlanningStatus.PLANNED);
        assertThat(next.getPlannedStartDate()).isEqualTo(expectedNextDate);
        assertThat(next.getPlannedEndDate()).isEqualTo(expectedNextDate.plusDays(7));
        assertThat(next.getNextPlannedDate()).isEqualTo(expectedNextDate);
    }

    @Test
    void cancel_marks_planning_cancelled() throws Exception {
        Truck truck = createTruck("T-002", "3A-5678");

        mockMvc.perform(post("/maintenance/save")
                .with(csrf())
                .param("truckId", truck.getId().toString())
                .param("planTitle", "Brake Check")
                .param("planTaskText", "Basic inspection")
                .param("plannedStartDate", "2026-02-10")
                .param("plannedEndDate", "2026-02-11")
                .param("intervalType", IntervalType.NONE.name()))
            .andExpect(status().is3xxRedirection());

        PlanningRepairMaintenance created = planningRepo.findAll().get(0);

        mockMvc.perform(post("/maintenance/cancel/{id}", created.getPlanningId())
                .with(csrf()))
            .andExpect(status().is3xxRedirection());

        PlanningRepairMaintenance cancelled = planningRepo.findById(created.getPlanningId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(PlanningStatus.CANCELLED);
        assertThat(planningRepo.count()).isEqualTo(1);
    }

    @Test
    void start_blocks_when_another_plan_in_progress_for_same_truck() throws Exception {
        Truck truck = createTruck("T-003", "3A-9999");

        PlanningRepairMaintenance first = planningRepo.save(buildPlan(truck, "Plan A"));
        PlanningRepairMaintenance second = planningRepo.save(buildPlan(truck, "Plan B"));

        mockMvc.perform(post("/maintenance/start/{id}", first.getPlanningId())
                .with(csrf()))
            .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/maintenance/start/{id}", second.getPlanningId())
                .with(csrf()))
            .andExpect(status().is4xxClientError());
    }

    private Truck createTruck(String code, String plate) {
        Model model = new Model();
        model.setCode("M-" + code);
        model.setName("Model " + code);
        model = modelRepo.save(model);

        Truck truck = new Truck();
        truck.setCode(code);
        truck.setLicensePlate(plate);
        truck.setModel(model);
        truck.setYear(2024);
        truck.setSize(TruckSize.BIG);
        return truckRepo.save(truck);
    }

    private PlanningRepairMaintenance buildPlan(Truck truck, String title) {
        PlanningRepairMaintenance plan = new PlanningRepairMaintenance();
        plan.setTruck(truck);
        plan.setPlanTitle(title);
        plan.setPlanTaskText("Task");
        plan.setPlannedStartDate(LocalDate.of(2026, 2, 1));
        plan.setPlannedEndDate(LocalDate.of(2026, 2, 2));
        plan.setIntervalType(IntervalType.NONE);
        plan.setStatus(PlanningStatus.PLANNED);
        return plan;
    }
}
