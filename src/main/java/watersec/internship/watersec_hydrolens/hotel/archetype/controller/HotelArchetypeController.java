package watersec.internship.watersec_hydrolens.hotel.archetype.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import watersec.internship.watersec_hydrolens.common.response.ApiResponse;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetypeCatalog;
import watersec.internship.watersec_hydrolens.hotel.archetype.dto.HotelArchetypeResponse;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hotel-archetypes")
public class HotelArchetypeController {
    private final HotelArchetypeCatalog catalog;
    public HotelArchetypeController(HotelArchetypeCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HotelArchetypeResponse>>> list() {
        List<HotelArchetypeResponse> archetypes = Arrays.stream(HotelArchetype.values())
                .map(code -> new HotelArchetypeResponse(code, catalog.get(code).displayName(),
                        catalog.get(code).description()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(archetypes, "Hotel archetypes retrieved successfully"));
    }
}
