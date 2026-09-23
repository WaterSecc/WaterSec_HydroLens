package watersec.internship.watersec_hydrolens.digitaltwin.session;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import watersec.internship.watersec_hydrolens.common.response.ApiResponse;
import watersec.internship.watersec_hydrolens.digitaltwin.session.dto.CreateDigitalTwinSessionRequest;
import watersec.internship.watersec_hydrolens.digitaltwin.session.service.DigitalTwinSessionService;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.NormalFlowSimulationRequest;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.SimulateFlowEventRequest;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/digital-twin-sessions")
public class DigitalTwinSessionController {
    private final DigitalTwinSessionService service;
    public DigitalTwinSessionController(DigitalTwinSessionService service) { this.service = service; }
    @PostMapping public ResponseEntity<ApiResponse<DigitalTwinSession>> create(@Valid @RequestBody CreateDigitalTwinSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "Temporary Digital Twin session created"));
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<DigitalTwinSession>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/{id}/baseline") public ResponseEntity<ApiResponse<DigitalTwinSession>> baseline(@PathVariable UUID id,
            @Valid @RequestBody(required = false) NormalFlowSimulationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.runBaseline(id, request), "Baseline simulated"));
    }
    @PostMapping("/{id}/events") public ResponseEntity<ApiResponse<DigitalTwinSession>> event(@PathVariable UUID id,
            @Valid @RequestBody SimulateFlowEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.simulateEvent(id, request), "Event simulated"));
    }
    @PostMapping("/{id}/scenario/reset") public ResponseEntity<ApiResponse<DigitalTwinSession>> reset(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.resetScenario(id), "Scenario reset"));
    }
}
