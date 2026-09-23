package watersec.internship.watersec_hydrolens.hotel.archetype.generator;

/** Optional caller-supplied values used while creating an in-memory hotel specification. */
public record HotelGenerationHints(String name, String country, String city, Long seed) {
}
