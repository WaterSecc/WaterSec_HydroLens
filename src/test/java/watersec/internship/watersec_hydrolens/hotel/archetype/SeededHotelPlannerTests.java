package watersec.internship.watersec_hydrolens.hotel.archetype;

import org.junit.jupiter.api.Test;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.HotelGenerationHints;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.SeededHotelPlanner;
import watersec.internship.watersec_hydrolens.hotel.archetype.generator.SyntheticHotelPlan;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeededHotelPlannerTests {
    private final HotelArchetypeCatalog catalog = new HotelArchetypeCatalog();
    private final SeededHotelPlanner planner = new SeededHotelPlanner(catalog);

    @Test
    void everyArchetypeProducesValidHotelValuesWithinItsDefinition() {
        for (HotelArchetype archetype : HotelArchetype.values()) {
            SyntheticHotelPlan plan = planner.plan(archetype, request(42));
            HotelArchetypeProfile profile = catalog.get(archetype);
            int rooms = plan.components().stream()
                    .filter(component -> component.type() == ComponentType.GUEST_ROOMS)
                    .findFirst().orElseThrow().quantity();

            assertTrue(rooms >= profile.rooms().minimum() && rooms <= profile.rooms().maximum());
            assertTrue(plan.floors() >= profile.floors().minimum() && plan.floors() <= profile.floors().maximum());
            assertTrue(plan.stars() >= profile.stars().minimum() && plan.stars() <= profile.stars().maximum());
            assertTrue(plan.occupancyRate() >= profile.occupancyRate().minimum());
            assertTrue(plan.occupancyRate() <= profile.occupancyRate().maximum());
            assertTrue(plan.employeeCount() > 0);
            assertFalse(plan.components().isEmpty());
            assertTrue(plan.components().stream().allMatch(component -> component.quantity() > 0));
            assertTrue(plan.components().stream().allMatch(component -> component.baseDailyConsumptionLiters() > 0));
        }
    }

    @Test
    void sameArchetypeAndSeedProduceIdenticalBusinessValues() {
        SyntheticHotelPlan first = planner.plan(HotelArchetype.RESORT, request(42));
        SyntheticHotelPlan second = planner.plan(HotelArchetype.RESORT, request(42));

        assertEquals(first, second);
    }

    @Test
    void differentSeedsVaryAHotelWithinTheSameDefinition() {
        SyntheticHotelPlan first = planner.plan(HotelArchetype.RESORT, request(42));
        SyntheticHotelPlan second = planner.plan(HotelArchetype.RESORT, request(43));

        assertNotEquals(first, second);
    }

    @Test
    void cityHotelAndResortProduceMeaningfullyDifferentConfigurations() {
        SyntheticHotelPlan city = planner.plan(HotelArchetype.CITY_HOTEL, request(42));
        SyntheticHotelPlan resort = planner.plan(HotelArchetype.RESORT, request(42));
        Set<ComponentType> cityTypes = types(city);
        Set<ComponentType> resortTypes = types(resort);

        assertFalse(cityTypes.contains(ComponentType.POOL));
        assertFalse(cityTypes.contains(ComponentType.COOLING_TOWER));
        assertTrue(resortTypes.contains(ComponentType.POOL));
        assertTrue(resortTypes.contains(ComponentType.IRRIGATION));
        assertTrue(resortTypes.contains(ComponentType.COOLING_TOWER));
        int cityIrrigation = quantity(city, ComponentType.IRRIGATION);
        int resortIrrigation = quantity(resort, ComponentType.IRRIGATION);
        assertTrue(cityIrrigation <= 300);
        assertTrue(resortIrrigation >= 4000);
        assertNotEquals(city.operatingProfile(), resort.operatingProfile());
    }

    @Test
    void generatedComponentsRespectArchetypeRulesAcrossSeeds() {
        for (long seed = 1; seed <= 100; seed++) {
            Set<ComponentType> city = types(planner.plan(HotelArchetype.CITY_HOTEL, request(seed)));
            Set<ComponentType> resort = types(planner.plan(HotelArchetype.RESORT, request(seed)));
            Set<ComponentType> aparthotel = types(planner.plan(HotelArchetype.APARTHOTEL, request(seed)));
            Set<ComponentType> luxury = types(planner.plan(HotelArchetype.LUXURY_RESORT, request(seed)));

            assertEquals(Set.of(ComponentType.GUEST_ROOMS), city.stream()
                    .filter(type -> type == ComponentType.GUEST_ROOMS).collect(Collectors.toSet()));
            assertFalse(city.contains(ComponentType.POOL));
            assertFalse(city.contains(ComponentType.COOLING_TOWER));
            assertTrue(resort.containsAll(Set.of(ComponentType.GUEST_ROOMS, ComponentType.LAUNDRY,
                    ComponentType.POOL, ComponentType.IRRIGATION, ComponentType.COOLING_TOWER)));
            assertTrue(aparthotel.contains(ComponentType.KITCHEN));
            assertTrue(luxury.containsAll(Set.of(ComponentType.POOL, ComponentType.SPA,
                    ComponentType.IRRIGATION, ComponentType.COOLING_TOWER)));
        }
    }

    private HotelGenerationHints request(long seed) {
        return new HotelGenerationHints(null, null, null, seed);
    }

    private Set<ComponentType> types(SyntheticHotelPlan plan) {
        return plan.components().stream().map(component -> component.type()).collect(Collectors.toSet());
    }

    private int quantity(SyntheticHotelPlan plan, ComponentType type) {
        return plan.components().stream().filter(component -> component.type() == type)
                .mapToInt(component -> component.quantity()).findFirst().orElse(0);
    }
}
