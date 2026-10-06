package dev.gazerah.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tenantAToken;
    private String tenantBToken;
    private String staffAEmail;
    private Long tenantAServiceId;
    private Long tenantAStaffId;
    private Long tenantBStaffId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        tenantAToken = signup("Salon " + suffix, "ownerA-" + suffix + "@example.com");
        tenantBToken = signup("Salon " + suffix + " B", "ownerB-" + suffix + "@example.com");

        tenantAServiceId = createService(tenantAToken, "Cut", 30);
        tenantAStaffId = createStaff(tenantAToken, staffAEmail = "staffA-" + suffix + "@example.com");
        tenantBStaffId = createStaff(tenantBToken, "staffB-" + suffix + "@example.com");
    }

    @Test
    void createsAndListsAppointments() throws Exception {
        createAppointment(tenantAToken, tenantAServiceId, tenantAStaffId,
                "2026-11-01T10:00:00Z", "2026-11-01T11:00:00Z")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/appointments").header("Authorization", bearer(tenantAToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void rejectsOverlappingBooking() throws Exception {
        createAppointment(tenantAToken, tenantAServiceId, tenantAStaffId,
                "2026-11-01T10:00:00Z", "2026-11-01T11:00:00Z")
                .andExpect(status().isCreated());

        createAppointment(tenantAToken, tenantAServiceId, tenantAStaffId,
                "2026-11-01T10:30:00Z", "2026-11-01T11:30:00Z")
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsCrossTenantStaffReference() throws Exception {
        createAppointment(tenantAToken, tenantAServiceId, tenantBStaffId,
                "2026-11-01T10:00:00Z", "2026-11-01T11:00:00Z")
                .andExpect(status().isNotFound());
    }

    @Test
    void staffCannotCreateService() throws Exception {
        String staffToken = login(staffAEmail, "password123");

        String body = "{\"name\":\"X\",\"durationMinutes\":30,\"price\":10.00}";

        mockMvc.perform(post("/api/services")
                        .header("Authorization", bearer(staffToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());
    }

    private String signup(String tenantName, String email) throws Exception {
        String body = "{\"tenantName\":\"%s\",\"fullName\":\"Owner\",\"email\":\"%s\",\"password\":\"password123\"}"
                .formatted(tenantName, email);
        return readToken(mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated()));
    }

    private String login(String email, String password) throws Exception {
        String body = "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
        return readToken(mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk()));
    }

    private String readToken(ResultActions actions) throws Exception {
        JsonNode json = objectMapper.readTree(actions.andReturn().getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private Long createService(String token, String name, int durationMinutes) throws Exception {
        String body = "{\"name\":\"%s\",\"durationMinutes\":%d,\"price\":50.00}"
                .formatted(name, durationMinutes);
        JsonNode json = objectMapper.readTree(mockMvc.perform(post("/api/services")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private Long createStaff(String token, String email) throws Exception {
        String body = "{\"fullName\":\"Staff\",\"email\":\"%s\",\"password\":\"password123\",\"role\":\"STAFF\"}"
                .formatted(email);
        JsonNode json = objectMapper.readTree(mockMvc.perform(post("/api/users")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private ResultActions createAppointment(String token, Long serviceId, Long staffId,
                                            String startAt, String endAt) throws Exception {
        String body = """
                {"serviceId":%d,"staffId":%d,"customerName":"Ana","startAt":"%s","endAt":"%s"}
                """.formatted(serviceId, staffId, startAt, endAt);
        return mockMvc.perform(post("/api/appointments")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
