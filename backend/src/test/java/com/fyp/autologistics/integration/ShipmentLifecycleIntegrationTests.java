package com.fyp.autologistics.integration;

import com.fyp.autologistics.entity.AppUser;
import com.fyp.autologistics.entity.ShipmentEvent;
import com.fyp.autologistics.entity.ShipmentPartyProfile;
import com.fyp.autologistics.entity.VehicleModel;
import com.fyp.autologistics.enums.AlertType;
import com.fyp.autologistics.enums.ShipmentEventSourceType;
import com.fyp.autologistics.enums.ShipmentEventType;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.repository.AdminNotificationRepository;
import com.fyp.autologistics.repository.AlertRepository;
import com.fyp.autologistics.repository.AppUserRepository;
import com.fyp.autologistics.repository.BlockchainRecordRepository;
import com.fyp.autologistics.repository.ShipmentEventRepository;
import com.fyp.autologistics.repository.ShipmentPartyProfileRepository;
import com.fyp.autologistics.repository.VehicleModelRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShipmentLifecycleIntegrationTests {

    private static final String MANUFACTURER_EMAIL = "manufacturer@integration.invalid";
    private static final String ADMIN_EMAIL = "admin@integration.invalid";
    private static final String LOGISTICS_EMAIL = "logistics@integration.invalid";
    private static final String DEALERSHIP_EMAIL = "dealership@integration.invalid";
    private static final String TEST_PASSWORD = "Integration-Only-Password-42";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    @Autowired
    private ShipmentPartyProfileRepository shipmentPartyProfileRepository;

    @Autowired
    private ShipmentEventRepository shipmentEventRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private BlockchainRecordRepository blockchainRecordRepository;

    @Autowired
    private AdminNotificationRepository adminNotificationRepository;

    @Autowired
    private Clock applicationClock;

    @Test
    void authenticatedShipmentJourneyPreservesAuditAlertsAndSkipsDisabledBlockchain()
            throws Exception {
        createReferenceDataAndUsers();

        String manufacturerToken = login(MANUFACTURER_EMAIL);
        String adminToken = login(ADMIN_EMAIL);
        String logisticsToken = login(LOGISTICS_EMAIL);
        String dealershipToken = login(DEALERSHIP_EMAIL);
        String estimatedArrival = LocalDateTime.now(applicationClock)
                .plusDays(1)
                .withNano(0)
                .toString();

        String createResponse = mockMvc.perform(post("/api/shipments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(manufacturerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleVin": "WDD-INTEGRATION-00001",
                                  "vehicleModelKey": "MODEL-INTEGRATION",
                                  "manufacturerKey": "MFG-INTEGRATION",
                                  "dealershipKey": "DLR-INTEGRATION",
                                  "estimatedArrival": "%s",
                                  "currentStage": "STOCK_YARD",
                                  "stockLocation": "Integration stock yard",
                                  "workInProgressStatus": "COMPLETED",
                                  "responsiblePerson": "Integration Operator",
                                  "responsibleContact": "+60000000000",
                                  "customerName": "Integration Customer",
                                  "customerContact": "+60000000001",
                                  "customerAddress": "Integration-only address"
                                }
                                """.formatted(estimatedArrival)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.manufacturerKey").value("MFG-INTEGRATION"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number shipmentIdValue = JsonPath.read(createResponse, "$.id");
        long shipmentId = shipmentIdValue.longValue();
        String shipmentCode = JsonPath.read(createResponse, "$.shipmentCode");
        String estimatedDepartureValue = JsonPath.read(
                createResponse,
                "$.estimatedDeparture"
        );
        assertThat(shipmentCode).startsWith("SHP-");
        assertThat(LocalDateTime.parse(estimatedDepartureValue).toLocalDate())
                .isEqualTo(LocalDate.now(applicationClock));

        mockMvc.perform(get("/api/shipments/{shipmentId}", shipmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(manufacturerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentCode").value(shipmentCode));

        mockMvc.perform(put("/api/shipments/{shipmentId}/status", shipmentId)
                        .queryParam("status", "READY_FOR_DISPATCH")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY_FOR_DISPATCH"))
                .andExpect(jsonPath("$.currentStage").value("READY_FOR_DISPATCH"));

        updateStatusAsLogistics(shipmentId, logisticsToken, "PICKED_UP");
        updateStatusAsLogistics(shipmentId, logisticsToken, "IN_TRANSIT");
        updateStatusAsLogistics(shipmentId, logisticsToken, "ARRIVED");

        mockMvc.perform(put("/api/shipments/{shipmentId}/status", shipmentId)
                        .queryParam("status", "DELIVERED")
                        .header(HttpHeaders.AUTHORIZATION, bearer(logisticsToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "A shipment becomes delivered only when its assigned dealership confirms receipt."
                ));

        mockMvc.perform(put("/api/shipments/{shipmentId}/receipt-confirmation", shipmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(logisticsToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmedBy": "Logistics must not confirm receipt"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/shipments/{shipmentId}/receipt-confirmation", shipmentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(dealershipToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmedBy": "Client-supplied name is not trusted"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.currentStage").value("DELIVERED"))
                .andExpect(jsonPath("$.receiptConfirmedBy").value("Integration DEALERSHIP"));

        List<ShipmentEvent> history = shipmentEventRepository
                .findByShipmentIdOrderByCreatedAtDesc(shipmentId);
        assertThat(history)
                .extracting(ShipmentEvent::getEventType)
                .contains(
                        ShipmentEventType.SHIPMENT_CREATED,
                        ShipmentEventType.STATUS_CHANGED,
                        ShipmentEventType.DELIVERY_CONFIRMED
                );
        assertThat(history)
                .filteredOn(event -> event.getEventType() == ShipmentEventType.STATUS_CHANGED
                        && "READY_FOR_DISPATCH".equals(event.getNewStatus()))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getActorEmail()).isEqualTo(ADMIN_EMAIL);
                    assertThat(event.getSourceType())
                            .isEqualTo(ShipmentEventSourceType.USER_ACTION);
                });

        assertThat(alertRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId))
                .extracting(alert -> alert.getAlertType())
                .contains(AlertType.ORDER_UPDATE);

        assertThat(blockchainRecordRepository
                .findByShipmentIdOrderByRecordedAtDesc(shipmentId))
                .isEmpty();
        assertThat(adminNotificationRepository.count()).isZero();
    }

    private String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void createReferenceDataAndUsers() {
        VehicleModel vehicleModel = new VehicleModel();
        vehicleModel.setModelCode("MODEL-INTEGRATION");
        vehicleModel.setBrand("Integration Brand");
        vehicleModel.setDisplayName("Integration Model");
        vehicleModel.setActive(true);
        vehicleModel.setSortOrder(1);
        vehicleModelRepository.save(vehicleModel);

        shipmentPartyProfileRepository.save(buildParty(
                "MFG-INTEGRATION",
                UserRole.MANUFACTURER,
                "Integration Manufacturer",
                "Integration Origin",
                3.1390,
                101.6869
        ));
        shipmentPartyProfileRepository.save(buildParty(
                "DLR-INTEGRATION",
                UserRole.DEALERSHIP,
                "Integration Dealership",
                "Integration Destination",
                5.4141,
                100.3288
        ));

        appUserRepository.save(buildUser(
                MANUFACTURER_EMAIL,
                UserRole.MANUFACTURER,
                "MFG-INTEGRATION"
        ));
        appUserRepository.save(buildUser(ADMIN_EMAIL, UserRole.ADMIN, null));
        appUserRepository.save(buildUser(
                LOGISTICS_EMAIL,
                UserRole.LOGISTICS_PROVIDER,
                null
        ));
        appUserRepository.save(buildUser(
                DEALERSHIP_EMAIL,
                UserRole.DEALERSHIP,
                "DLR-INTEGRATION"
        ));
    }

    private void updateStatusAsLogistics(
            long shipmentId,
            String logisticsToken,
            String nextStatus
    ) throws Exception {
        mockMvc.perform(put("/api/shipments/{shipmentId}/status", shipmentId)
                        .queryParam("status", nextStatus)
                        .header(HttpHeaders.AUTHORIZATION, bearer(logisticsToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(nextStatus));
    }

    private ShipmentPartyProfile buildParty(
            String key,
            UserRole role,
            String companyName,
            String location,
            double latitude,
            double longitude
    ) {
        ShipmentPartyProfile profile = new ShipmentPartyProfile();
        profile.setPartyKey(key);
        profile.setPartyRole(role);
        profile.setCompanyName(companyName);
        profile.setOperatingLocation(location);
        profile.setLocationLatitude(latitude);
        profile.setLocationLongitude(longitude);
        profile.setActive(true);
        profile.setSortOrder(1);
        return profile;
    }

    private AppUser buildUser(String email, UserRole role, String partyKey) {
        AppUser user = new AppUser();
        user.setFullName("Integration " + role.name());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        user.setRole(role);
        user.setActive(true);
        user.setPartyProfileKey(partyKey);
        return user;
    }
}
