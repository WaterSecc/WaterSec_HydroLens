package watersec.internship.watersec_hydrolens.component.entity;

/**
 * Categories of water-consuming subsystem a facility can contain. Persisted as a
 * string; extend as new subsystem types are modelled by the simulation engine.
 */
public enum ComponentType {
    GUEST_ROOMS,
    POOL,
    LAUNDRY,
    RESTAURANT,
    KITCHEN,
    COOLING_TOWER,
    IRRIGATION,
    SPA,
    OTHER
}
