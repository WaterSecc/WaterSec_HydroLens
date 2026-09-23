package watersec.internship.watersec_hydrolens.simulation.event;

import java.time.Instant;

public record OccupancySnapshot(Instant timestamp, int occupiedUnits, int guests, double occupancyRate) {
}
