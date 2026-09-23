package watersec.internship.watersec_hydrolens.hotel.archetype;

public record HotelArchetypeProfile(
        String displayName,
        String description,
        IntRange rooms,
        IntRange floors,
        IntRange stars,
        DoubleRange occupancyRate,
        IntRange restaurants,
        double internalLaundryProbability,
        IntRange pools,
        double spaProbability,
        double irrigationProbability,
        IntRange irrigationAreaSquareMeters,
        IntRange coolingTowers,
        DoubleRange employeesPerRoom,
        OperatingProfile operatingProfile,
        boolean apartmentKitchens) {

    public HotelArchetypeProfile {
        if (displayName == null || displayName.isBlank() || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Archetype name and description are required");
        }
        probability(internalLaundryProbability, "internalLaundryProbability");
        probability(spaProbability, "spaProbability");
        probability(irrigationProbability, "irrigationProbability");
        if (rooms.minimum() < 1 || floors.minimum() < 1 || stars.minimum() < 1 || stars.maximum() > 5) {
            throw new IllegalArgumentException("Hotel ranges must describe a valid hotel");
        }
        if (occupancyRate.minimum() < 0 || occupancyRate.maximum() > 1) {
            throw new IllegalArgumentException("Occupancy range must be between 0 and 1");
        }
    }

    private static void probability(double value, String field) {
        if (value < 0 || value > 1) throw new IllegalArgumentException(field + " must be between 0 and 1");
    }

    public record IntRange(int minimum, int maximum) {
        public IntRange {
            if (minimum < 0 || minimum > maximum) throw new IllegalArgumentException("Invalid integer range");
        }
    }

    public record DoubleRange(double minimum, double maximum) {
        public DoubleRange {
            if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum < 0 || minimum > maximum) {
                throw new IllegalArgumentException("Invalid decimal range");
            }
        }
    }
}
