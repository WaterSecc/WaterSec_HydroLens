package watersec.internship.watersec_hydrolens.hotel.archetype.generator;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetypeCatalog;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetypeProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class SeededHotelPlanner {
    public static final long DEFAULT_SEED = 42_017L;
    private static final int REFERENCE_YEAR = 2026;
    private final HotelArchetypeCatalog catalog;

    public SeededHotelPlanner(HotelArchetypeCatalog catalog) {
        this.catalog = catalog;
    }

    public SyntheticHotelPlan plan(HotelArchetype archetype, HotelGenerationHints request) {
        HotelGenerationHints input = request == null ? new HotelGenerationHints(null, null, null, null) : request;
        long seed = input.seed() == null ? DEFAULT_SEED : input.seed();
        Random random = new Random(seed ^ ((long) archetype.name().hashCode() << 32));
        HotelArchetypeProfile profile = catalog.get(archetype);
        int rooms = integer(profile.rooms(), random);
        int floors = integer(profile.floors(), random);
        int stars = integer(profile.stars(), random);
        double occupancy = decimal(profile.occupancyRate(), random);
        int restaurants = integer(profile.restaurants(), random);
        boolean internalLaundry = random.nextDouble() < profile.internalLaundryProbability();
        int pools = integer(profile.pools(), random);
        boolean spa = random.nextDouble() < profile.spaProbability();
        boolean irrigated = random.nextDouble() < profile.irrigationProbability();
        int irrigationArea = irrigated ? integer(profile.irrigationAreaSquareMeters(), random) : 0;
        int coolingTowers = integer(profile.coolingTowers(), random);
        int employees = (int) Math.round(rooms * decimal(profile.employeesPerRoom(), random));

        List<SyntheticComponentPlan> components = new ArrayList<>();
        components.add(component(ComponentType.GUEST_ROOMS, "Guest Rooms", rooms,
                profile.apartmentKitchens() ? 360 : 300, "rooms", true, .72));
        if (internalLaundry) {
            int machines = Math.max(2, (int) Math.ceil(rooms / 45.0));
            components.add(component(ComponentType.LAUNDRY, "Internal Laundry", machines,
                    round(120.0 * rooms * occupancy * .7 / machines), "machines", true, .68));
        }
        if (restaurants > 0) {
            components.add(component(ComponentType.RESTAURANT, "Restaurants", restaurants,
                    round(rooms * occupancy * 24.0 / restaurants), "restaurants", true, .70));
            if (!profile.apartmentKitchens()) {
                components.add(component(ComponentType.KITCHEN, "Commercial Kitchens", restaurants,
                        round(rooms * occupancy * 16.0 / restaurants), "kitchens", true, .66));
            }
        }
        if (profile.apartmentKitchens()) {
            components.add(component(ComponentType.KITCHEN, "Apartment Kitchens", rooms,
                    42, "kitchens", true, .74));
        }
        if (pools > 0) {
            components.add(component(ComponentType.POOL, "Swimming Pools", pools,
                    archetype == HotelArchetype.LUXURY_RESORT ? 1800 : 1300, "pools", false, .64));
        }
        if (spa) {
            components.add(component(ComponentType.SPA, "Spa", 1,
                    Math.max(1400, rooms * 22.0), "spa", true, .62));
        }
        if (irrigationArea > 0) {
            components.add(component(ComponentType.IRRIGATION, "Landscape Irrigation", irrigationArea,
                    5, "m2", false, .58));
        }
        if (coolingTowers > 0) {
            components.add(component(ComponentType.COOLING_TOWER, "HVAC Cooling System", coolingTowers,
                    round(floors * 500.0 / coolingTowers), "cooling towers", false, .67));
        }

        return new SyntheticHotelPlan(archetype, seed,
                valueOrDefault(input.name(), generatedName(profile, random)),
                valueOrDefault(input.country(), "Tunisia"), valueOrDefault(input.city(), defaultCity(archetype)),
                stars, floors, REFERENCE_YEAR - 4 - random.nextInt(27), round(occupancy), .003, employees,
                profile.operatingProfile(), List.copyOf(components));
    }

    private SyntheticComponentPlan component(ComponentType type, String name, int quantity, double consumption,
                                             String unit, boolean occupancyDependent, double efficiency) {
        return new SyntheticComponentPlan(type, name, quantity, round(consumption), unit, occupancyDependent, efficiency);
    }

    private int integer(HotelArchetypeProfile.IntRange range, Random random) {
        return range.minimum() + random.nextInt(range.maximum() - range.minimum() + 1);
    }

    private double decimal(HotelArchetypeProfile.DoubleRange range, Random random) {
        return range.minimum() + random.nextDouble() * (range.maximum() - range.minimum());
    }

    private String generatedName(HotelArchetypeProfile profile, Random random) {
        String[] prefixes = {"Azure", "Carthage", "Olive", "Medina", "Sapphire", "Jasmine"};
        return prefixes[random.nextInt(prefixes.length)] + " " + profile.displayName();
    }

    private String defaultCity(HotelArchetype archetype) {
        return switch (archetype) {
            case CITY_HOTEL, BUSINESS_HOTEL, BOUTIQUE_HOTEL, APARTHOTEL -> "Tunis";
            case RESORT -> "Hammamet";
            case LUXURY_RESORT -> "Djerba";
        };
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private double round(double value) {
        return Math.round(value * 10_000.0) / 10_000.0;
    }
}
