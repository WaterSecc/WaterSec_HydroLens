package watersec.internship.watersec_hydrolens.simulation.normalflow.engine;

import watersec.internship.watersec_hydrolens.hotel.archetype.HotelArchetype;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.NormalFlowEvent;

import java.util.List;

public record NormalFlowInput(
        HotelArchetype archetype,
        double occupancyRate,
        long seed,
        int currentHour,
        List<NormalFlowComponent> components,
        NormalFlowEvent event) {

    public NormalFlowInput(HotelArchetype archetype, double occupancyRate, long seed,
                           int currentHour, List<NormalFlowComponent> components) {
        this(archetype, occupancyRate, seed, currentHour, components, null);
    }
}
