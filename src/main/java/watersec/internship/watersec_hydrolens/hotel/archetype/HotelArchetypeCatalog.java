package watersec.internship.watersec_hydrolens.hotel.archetype;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class HotelArchetypeCatalog {
    private final Map<HotelArchetype, HotelArchetypeProfile> profiles = profiles();

    public HotelArchetypeProfile get(HotelArchetype archetype) {
        return profiles.get(archetype);
    }

    public Map<HotelArchetype, HotelArchetypeProfile> getAll() {
        return Map.copyOf(profiles);
    }

    private Map<HotelArchetype, HotelArchetypeProfile> profiles() {
        Map<HotelArchetype, HotelArchetypeProfile> result = new EnumMap<>(HotelArchetype.class);
        result.put(HotelArchetype.CITY_HOTEL, new HotelArchetypeProfile(
                "City Hotel", "Compact urban hotel with room-led water demand.",
                r(80, 180), r(5, 14), r(3, 4), d(.68, .86), r(0, 1), .65,
                r(0, 0), .05, .15, r(100, 300), r(0, 0), d(.55, .85), OperatingProfile.URBAN_WEEKDAY, false));
        result.put(HotelArchetype.BUSINESS_HOTEL, new HotelArchetypeProfile(
                "Business Hotel", "Weekday-focused hotel with conference and restaurant demand.",
                r(140, 300), r(7, 18), r(3, 5), d(.70, .88), r(1, 2), .80,
                r(0, 1), .10, .25, r(150, 500), r(1, 2), d(.65, 1.0), OperatingProfile.BUSINESS_WEEKDAY, false));
        result.put(HotelArchetype.RESORT, new HotelArchetypeProfile(
                "Resort", "Leisure property with pools, landscaping, restaurants and spa.",
                r(180, 420), r(3, 9), r(4, 5), d(.65, .88), r(2, 4), 1.0,
                r(1, 3), .80, 1.0, r(4000, 12000), r(1, 3), d(.80, 1.30), OperatingProfile.LEISURE_SEASONAL, false));
        result.put(HotelArchetype.BOUTIQUE_HOTEL, new HotelArchetypeProfile(
                "Boutique Hotel", "Small premium hotel with personalized guest services.",
                r(30, 90), r(2, 7), r(3, 5), d(.58, .82), r(1, 1), .55,
                r(0, 1), .35, .35, r(100, 800), r(0, 1), d(.70, 1.10), OperatingProfile.BOUTIQUE_BALANCED, false));
        result.put(HotelArchetype.APARTHOTEL, new HotelArchetypeProfile(
                "Aparthotel", "Long-stay accommodation with higher in-room and kitchen demand.",
                r(60, 180), r(4, 12), r(3, 4), d(.62, .85), r(0, 1), .35,
                r(0, 1), .10, .30, r(150, 1000), r(0, 1), d(.35, .65), OperatingProfile.EXTENDED_STAY, true));
        result.put(HotelArchetype.LUXURY_RESORT, new HotelArchetypeProfile(
                "Luxury Resort", "Large five-star destination with extensive leisure assets.",
                r(250, 600), r(4, 12), r(5, 5), d(.68, .90), r(3, 7), 1.0,
                r(2, 5), 1.0, 1.0, r(8000, 25000), r(2, 5), d(1.10, 1.80), OperatingProfile.LUXURY_LEISURE, false));
        return result;
    }

    private HotelArchetypeProfile.IntRange r(int minimum, int maximum) {
        return new HotelArchetypeProfile.IntRange(minimum, maximum);
    }

    private HotelArchetypeProfile.DoubleRange d(double minimum, double maximum) {
        return new HotelArchetypeProfile.DoubleRange(minimum, maximum);
    }
}
