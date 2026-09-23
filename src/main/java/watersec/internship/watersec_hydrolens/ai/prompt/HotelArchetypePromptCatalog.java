package watersec.internship.watersec_hydrolens.ai.prompt;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;

/**
 * Stores the additional prompt guidance for each supported hotel archetype.
 *
 * <p>The common safety, output, and schema instructions remain in the Llama
 * gateway. These prompts should contain only archetype-specific domain context:
 * typical services, operating patterns, plausible assets, and assumptions.</p>
 */
@Component
public class HotelArchetypePromptCatalog {

    // Add the City Hotel-specific guidance between these text-block delimiters.
    private static final String CITY_HOTEL_PROMPT = """
            You are generating a realistic CITY_HOTEL configuration for the WaterSec HydroLens synthetic Digital Twin.
            
            A city hotel is primarily an urban accommodation property designed around guest rooms, business/leisure stays, food service, and relatively compact building footprints.
            
            Typical characteristics:
            - Usually located in a dense urban area.
            - Guest-room accommodation is the dominant facility function.
            - Room count is typically moderate to high.
            - Building footprint is relatively compact compared with resorts.
            - Multiple floors are common.
            - Outdoor landscaped areas are usually limited.
            - Large irrigation systems should be uncommon.
            - Swimming pools are possible, but should not be assumed.
            - Spa facilities may exist in upper-category hotels, but should not be included automatically.
            - One or more restaurants or kitchens are common.
            - Laundry may be internal or outsourced.
            - Cooling systems are common, but water-consuming cooling towers should only be included when plausible.
            - Water demand should be driven mostly by guest rooms, bathrooms, kitchens, restaurants, laundry, and common areas.
            
            Generation guidance:
            - Prioritize GUEST_ROOMS.
            - Include RESTAURANT or KITCHEN when justified.
            - Include LAUNDRY only when appropriate for hotel size and category.
            - Avoid large IRRIGATION components unless explicitly justified.
            - Avoid multiple POOL components unless the property clearly behaves like an urban luxury hotel.
            - SPA is optional, not default.
            - Component mix should remain compact and realistic.
            - Do not transform a city hotel into a resort.
            - Keep room count, floor count, occupancy, and component quantities internally coherent.

            REFERENCE EXAMPLE (copy the reasoning and scale, not the identity):
            A 4-star, 140-room city hotel at 72% occupancy can use components such as:
            - GUEST_ROOMS: quantity 140 rooms, 280 L/room/day, occupancyDependent true, efficiency 0.72.
            - LAUNDRY: quantity 4 machines, 3000 L/machine/day, occupancyDependent true, efficiency 0.68.
            - RESTAURANT: quantity 1 restaurant, 3500 L/restaurant/day, occupancyDependent true, efficiency 0.70.
            - KITCHEN: quantity 1 kitchen, 2500 L/kitchen/day, occupancyDependent true, efficiency 0.66.
            - COOLING_TOWER: quantity 2 cooling towers, 3000 L/tower/day, occupancyDependent false, efficiency 0.67.
            Water cost example: 0.003 TND/L. Do not use tiny placeholder demands.
            
            The goal is to generate a believable urban hotel suitable for deterministic water simulation.
            """;

    // Add the Business Hotel-specific guidance between these text-block delimiters.
    private static final String BUSINESS_HOTEL_PROMPT = """
            You are generating a realistic BUSINESS_HOTEL configuration for the WaterSec HydroLens synthetic Digital Twin.
            
            A business hotel primarily serves corporate travelers, meetings, conferences, and short-duration stays.
            
            Typical characteristics:
            - Usually located in business districts, city centers, airports, or commercial areas.
            - Guest rooms are the main accommodation component.
            - Weekday activity is typically more important than leisure-seasonality patterns.
            - Occupancy can be relatively high during business days.
            - Meeting rooms, conference activity, restaurants, and breakfast service are common.
            - Food-service demand may be important.
            - Laundry may be internal or outsourced depending on hotel scale.
            - Large landscaped areas are uncommon.
            - Large resort-style pools and extensive spa facilities are uncommon unless explicitly justified.
            - Water use is mostly associated with guest rooms, bathrooms, kitchens, restaurants, conference activity, housekeeping, and laundry.
            - Cooling systems may be significant in large business hotels.
            
            Generation guidance:
            - Always include GUEST_ROOMS.
            - RESTAURANT and/or KITCHEN are usually appropriate.
            - LAUNDRY is plausible, especially for medium and large properties.
            - POOL should be uncommon and limited if present.
            - IRRIGATION should normally be absent or minimal.
            - SPA should not be included unless the hotel is positioned as an upper-end business hotel.
            - COOLING_TOWER may be included for larger buildings if plausible.
            - Avoid resort-style amenities.
            - Keep the configuration focused on accommodation + business/conference-related operations.

            REFERENCE EXAMPLE (copy the reasoning and scale, not the identity):
            A 4-star, 180-room business hotel at 76% occupancy can use:
            - GUEST_ROOMS: 180 rooms at 270 L/room/day.
            - LAUNDRY: 5 machines at 3200 L/machine/day.
            - RESTAURANT: 2 restaurants at 3200 L/restaurant/day.
            - KITCHEN: 2 kitchens at 2400 L/kitchen/day.
            - COOLING_TOWER: 2 cooling towers at 4000 L/tower/day.
            Occupancy-dependent services should be marked true; cooling towers false. Use efficiencies around 0.60-0.80 and water cost around 0.003 TND/L.
            
            The generated hotel should feel like a professional urban/commercial property rather than a leisure resort.
            """;

    // Add the Resort-specific guidance between these text-block delimiters.
    private static final String RESORT_PROMPT = """
            You are generating a realistic RESORT configuration for the WaterSec HydroLens synthetic Digital Twin.
            
            A resort is a leisure-oriented hotel with a broader range of water-consuming amenities and a stronger dependence on outdoor facilities and seasonality.
            
            Typical characteristics:
            - Usually larger than boutique or city hotels.
            - Guest rooms remain the main accommodation component.
            - Swimming pools are common.
            - Landscaped outdoor areas and irrigation are common.
            - Multiple restaurants or kitchens are common.
            - Internal laundry is common.
            - Spa/wellness facilities are plausible.
            - Outdoor water demand can be substantial.
            - Pools may require refill/makeup water.
            - Irrigation can be an important part of total water demand.
            - Seasonal occupancy changes may be significant.
            - Cooling demand may be important in warm climates.
            - Water demand is typically more diversified than in city hotels.
            
            Generation guidance:
            - Always include GUEST_ROOMS.
            - Usually include RESTAURANT and/or KITCHEN.
            - Usually include LAUNDRY.
            - Include POOL in most generated resorts.
            - Include IRRIGATION when outdoor landscaping is plausible.
            - SPA is common but not mandatory.
            - COOLING_TOWER may be included when appropriate.
            - Component quantities should scale sensibly with hotel size.
            - Avoid creating an unrealistically large number of pools, restaurants, or other amenities.
            - Keep all generated assets coherent with room count and hotel scale.

            REFERENCE EXAMPLE (copy the reasoning and scale, not the identity):
            A 4-star, 260-room resort at 74% occupancy can use:
            - GUEST_ROOMS: 260 rooms at 320 L/room/day.
            - LAUNDRY: 7 machines at 3500 L/machine/day.
            - RESTAURANT: 3 restaurants at 4200 L/restaurant/day.
            - KITCHEN: 3 kitchens at 3000 L/kitchen/day.
            - POOL: 2 pools at 2500 L/pool/day of makeup/backwash water.
            - SPA: 1 spa at 4500 L/spa/day.
            - IRRIGATION: 5000 m2 at 5 L/m2/day.
            - COOLING_TOWER: 3 towers at 4500 L/tower/day.
            Use realistic efficiencies around 0.55-0.80 and water cost around 0.003 TND/L.
            
            The goal is a realistic leisure resort with diversified water demand and meaningful outdoor water use.
            """;

    // Add the Boutique Hotel-specific guidance between these text-block delimiters.
    private static final String BOUTIQUE_HOTEL_PROMPT = """
            You are generating a realistic BOUTIQUE_HOTEL configuration for the WaterSec HydroLens synthetic Digital Twin.
            
            A boutique hotel is typically smaller, design-oriented, and more individualized than large chain hotels.
            
            Typical characteristics:
            - Small to medium room count.
            - Fewer floors than large city/business hotels, although exceptions exist.
            - High service level is possible despite small scale.
            - One restaurant or kitchen is common but not guaranteed.
            - Laundry may be outsourced because of smaller scale.
            - Spa or small pool may exist in premium boutique properties.
            - Extensive irrigation, large pools, and large cooling infrastructure are usually inappropriate.
            - Component count should remain limited.
            - Water demand should mainly come from guest rooms, bathrooms, food service, housekeeping, and possibly one or two premium amenities.
            
            Generation guidance:
            - Always include GUEST_ROOMS.
            - Keep the number of additional components low.
            - RESTAURANT or KITCHEN may be included.
            - LAUNDRY may be absent or outsourced conceptually; only include it when plausible.
            - POOL should be limited to small-scale cases.
            - SPA is possible in upscale boutique hotels, but not automatic.
            - IRRIGATION should be absent or minimal unless the property has meaningful outdoor grounds.
            - COOLING_TOWER should be rare.
            - Avoid making the hotel look like a resort or large convention property.

            REFERENCE EXAMPLE (copy the reasoning and scale, not the identity):
            A 4-star, 45-room boutique hotel at 68% occupancy can use:
            - GUEST_ROOMS: 45 rooms at 300 L/room/day.
            - RESTAURANT: 1 restaurant at 1600 L/restaurant/day.
            - KITCHEN: 1 kitchen at 1200 L/kitchen/day.
            An internal laundry may be omitted. If included, 2 machines at about 1200 L/machine/day is plausible. A small spa may use 1 spa at 1200 L/spa/day. Do not add resort infrastructure without justification.
            
            The generated configuration should be compact, premium, and internally coherent.
            """;

    // Add the Aparthotel-specific guidance between these text-block delimiters.
    private static final String APARTHOTEL_PROMPT = """
            You are generating a realistic APARTHOTEL configuration for the WaterSec HydroLens synthetic Digital Twin.
            
            An aparthotel combines hotel accommodation with apartment-style units and longer average guest stays.
            
            Typical characteristics:
            - Guest units may include kitchenettes or kitchen facilities.
            - Length of stay is often longer than in conventional hotels.
            - Guest behavior may generate more in-room water use associated with cooking and washing.
            - Shared hotel restaurants may be less dominant than in full-service hotels.
            - Laundry can be internal, self-service, outsourced, or mixed.
            - Pools and spa facilities may exist but are not defining characteristics.
            - Irrigation is usually limited unless the aparthotel is in a resort-style setting.
            - Water consumption patterns should reflect apartment-style living rather than only short hotel stays.
            - Room/unit counts can vary significantly.
            
            Generation guidance:
            - Always include GUEST_ROOMS, treating them as apartment-style accommodation units.
            - Consider KITCHEN-related demand more strongly than in conventional hotels.
            - RESTAURANT is optional rather than assumed.
            - LAUNDRY is plausible and can be significant.
            - POOL may be included if appropriate but should not be assumed.
            - SPA is optional.
            - IRRIGATION should normally be limited.
            - COOLING_TOWER may be present in larger buildings.
            - Do not turn the property into a conventional resort.

            REFERENCE EXAMPLE (copy the reasoning and scale, not the identity):
            A 4-star, 110-unit aparthotel at 70% occupancy can use:
            - GUEST_ROOMS: 110 rooms at 300 L/room/day.
            - KITCHEN: 110 in-unit kitchens at 45 L/kitchen/day.
            - LAUNDRY: 4 machines at 2600 L/machine/day.
            - RESTAURANT: optionally 1 restaurant at 1800 L/restaurant/day.
            - COOLING_TOWER: 2 towers at 2800 L/tower/day when technically plausible.
            In-unit kitchens are the only case where many KITCHEN units with a small per-unit baseline are expected.
            
            The generated hotel should reflect longer-stay accommodation with stronger in-unit domestic water behavior.
            """;

    // Add the Luxury Resort-specific guidance between these text-block delimiters.
    private static final String LUXURY_RESORT_PROMPT = """
            You are generating a realistic LUXURY_RESORT configuration for the WaterSec HydroLens synthetic Digital Twin.
            
            A luxury resort is a high-end leisure property with extensive amenities and diversified water use.
            
            Typical characteristics:
            - Medium to very large room count.
            - High service intensity.
            - Multiple restaurants and kitchens are common.
            - Internal laundry is common.
            - Multiple pools are plausible.
            - Spa and wellness facilities are common.
            - Landscaped areas and irrigation can be extensive.
            - Cooling demand may be important.
            - Water use per occupied room can be higher than in simpler hotel categories because of amenities and service levels.
            - Outdoor water demand can be a major part of total consumption.
            - Multiple water-consuming subsystems are expected, but they must remain coherent with hotel scale.
            
            Generation guidance:
            - Always include GUEST_ROOMS.
            - Usually include RESTAURANT and KITCHEN.
            - Include LAUNDRY in most cases.
            - Include POOL, often with more than one pool when hotel size justifies it.
            - Include SPA in most cases.
            - Include IRRIGATION when significant landscaping is plausible.
            - COOLING_TOWER may be included for larger facilities.
            - Avoid adding every possible component purely because the hotel is luxury.
            - Scale quantities reasonably with room count and hotel size.
            - Do not create implausible combinations such as dozens of pools or restaurants for a small property.
            - Preserve internal consistency above complexity.

            REFERENCE EXAMPLE (copy the reasoning and scale, not the identity):
            A 5-star, 420-room luxury resort at 78% occupancy can use:
            - GUEST_ROOMS: 420 rooms at 380 L/room/day.
            - LAUNDRY: 10 machines at 4500 L/machine/day.
            - RESTAURANT: 5 restaurants at 5000 L/restaurant/day.
            - KITCHEN: 5 kitchens at 3800 L/kitchen/day.
            - POOL: 4 pools at 3500 L/pool/day of makeup/backwash water.
            - SPA: 2 spas at 5500 L/spa/day.
            - IRRIGATION: 12000 m2 at 6 L/m2/day.
            - COOLING_TOWER: 5 towers at 5500 L/tower/day.
            Use service-intensive but defensible values, efficiencies around 0.55-0.80, and water cost around 0.003 TND/L.
            
            The generated configuration should represent a premium leisure facility with diversified and comparatively high water demand.
            """;

    /** Returns exactly one prompt for the selected archetype. */
    public String promptFor(HotelArchetype archetype) {
        if (archetype == null) throw new IllegalArgumentException("Hotel archetype is required");
        return switch (archetype) {
            case CITY_HOTEL -> CITY_HOTEL_PROMPT;
            case BUSINESS_HOTEL -> BUSINESS_HOTEL_PROMPT;
            case RESORT -> RESORT_PROMPT;
            case BOUTIQUE_HOTEL -> BOUTIQUE_HOTEL_PROMPT;
            case APARTHOTEL -> APARTHOTEL_PROMPT;
            case LUXURY_RESORT -> LUXURY_RESORT_PROMPT;
        };
    }
}
