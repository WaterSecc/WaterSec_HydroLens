package watersec.internship.watersec_hydrolens.simulation.normalflow.engine;

import org.springframework.stereotype.Component;
import watersec.internship.watersec_hydrolens.component.entity.ComponentType;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.WaterNetworkEdge;
import watersec.internship.watersec_hydrolens.simulation.normalflow.dto.WaterNetworkNode;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.EventSeverity;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.FlowEventType;
import watersec.internship.watersec_hydrolens.simulation.normalflow.event.NormalFlowEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class NormalHotelFlowEngine {

    private static final String SOURCE = "municipal-supply";
    private static final String STORAGE = "main-storage";

    /**
     * The simulation is intentionally reproducible.
     *
     * The generated timestamp is therefore derived from a fixed epoch + seed,
     * instead of Instant.now().
     */
    private static final Instant REPRODUCIBLE_EPOCH =
            Instant.parse("2026-01-01T00:00:00Z");

    /**
     * Occupancy-dependent components still have a small fixed demand even at
     * zero guest occupancy.
     *
     * This represents activities such as:
     * - housekeeping
     * - maintenance
     * - staff use
     * - cleaning
     * - preparation / standby operation
     *
     * At 100% occupancy:
     * 0.15 + 0.85 * 1.0 = 1.0
     */
    private static final double FIXED_OCCUPANCY_DEMAND_SHARE = 0.15;
    private static final double OCCUPANCY_DRIVEN_DEMAND_SHARE = 0.85;

    /**
     * Efficiency semantics:
     *
     * efficiencyRating = 0.0 -> inefficient -> factor 1.15
     * efficiencyRating = 0.5 -> average     -> factor 1.00
     * efficiencyRating = 1.0 -> efficient   -> factor 0.85
     *
     * This is intentionally simple and explainable for the MVP.
     */
    private static final double INEFFICIENT_OPERATION_FACTOR = 1.15;
    private static final double EFFICIENCY_FACTOR_RANGE = 0.30;

    /**
     * Small deterministic operational variability around the calculated
     * baseline.
     *
     * Produces a multiplier in [0.97, 1.03].
     */
    private static final double RANDOM_FACTOR_MIN = 0.97;
    private static final double RANDOM_FACTOR_RANGE = 0.06;

    /**
     * When municipal supply returns after an interruption, the storage tank
     * does not magically become full.
     *
     * By default we assume it can refill from empty in approximately 6 hours,
     * while the municipal source simultaneously supplies current hotel demand.
     */
    private static final double DEFAULT_STORAGE_REFILL_HOURS = 6.0;

    /**
     * Default continuous leak rate when no explicit rate is supplied.
     */
    private static final double DEFAULT_CONTINUOUS_LEAK_LPH = 100.0;

    /**
     * Default pipe-burst flows.
     *
     * These are simulation assumptions, not hydraulic calculations.
     *
     * A future hydraulic model could derive these from:
     * - pipe diameter
     * - network pressure
     * - rupture size
     * - discharge coefficient
     */
    private static final double PIPE_BURST_LOW_LPH = 500.0;
    private static final double PIPE_BURST_MEDIUM_LPH = 1_500.0;
    private static final double PIPE_BURST_HIGH_LPH = 4_000.0;

    private static final double EPSILON = 0.01;

    public NormalFlowResult simulate(NormalFlowInput input) {

        validateInput(input);

        /*
         * STEP 1
         *
         * Calculate normal demand for every water-consuming component.
         */
        List<ConsumerDemand> demands = input.components().stream()
                .map(component -> demand(component, input))
                .toList();

        /*
         * STEP 2
         *
         * Supply interruption is different from normal demand-side events.
         *
         * A leak changes demand.
         * A heat wave changes demand.
         *
         * A supply interruption instead limits how much of that demand
         * can actually be delivered.
         */
        SupplyOutcome supply = applySupplyInterruption(
                demands,
                input.event()
        );

        demands = supply.demands();

        /*
         * STEP 3
         *
         * Total delivered/consumed water after all event effects.
         */
        double totalDailyConsumption = round(
                demands.stream()
                        .mapToDouble(ConsumerDemand::dailyLiters)
                        .sum()
        );

        /*
         * STEP 4
         *
         * Aggregate component hourly flows.
         */
        List<Double> hourlyTotals = hourlyTotals(demands);

        /*
         * STEP 5
         *
         * Build network visualization objects.
         */
        List<WaterNetworkNode> nodes = nodes(
                demands,
                totalDailyConsumption,
                hourlyTotals,
                supply.sourceHourly(),
                input.currentHour()
        );

        List<WaterNetworkEdge> edges = edges(
                demands,
                hourlyTotals,
                supply.sourceHourly(),
                input.currentHour()
        );

        /*
         * Reproducible timestamp.
         */
        Instant timestamp = REPRODUCIBLE_EPOCH
                .plus(Math.floorMod(input.seed(), 365), ChronoUnit.DAYS)
                .plus(input.currentHour(), ChronoUnit.HOURS);

        return new NormalFlowResult(
                timestamp,
                totalDailyConsumption,
                hourlyTotals,
                nodes,
                edges,
                supply.storageLevels(),
                supply.remainingAutonomyHours()
        );
    }

    // -------------------------------------------------------------------------
    // INPUT VALIDATION
    // -------------------------------------------------------------------------

    private void validateInput(NormalFlowInput input) {

        if (input == null) {
            throw new IllegalArgumentException(
                    "Normal flow simulation input must not be null"
            );
        }

        if (input.components() == null || input.components().isEmpty()) {
            throw new IllegalArgumentException(
                    "Normal flow simulation requires at least one component"
            );
        }

        if (input.currentHour() < 0 || input.currentHour() > 23) {
            throw new IllegalArgumentException(
                    "currentHour must be between 0 and 23"
            );
        }

        if (input.occupancyRate() < 0 || input.occupancyRate() > 1) {
            throw new IllegalArgumentException(
                    "occupancyRate must be between 0 and 1"
            );
        }
    }

    // -------------------------------------------------------------------------
    // COMPONENT DEMAND
    // -------------------------------------------------------------------------

    private ConsumerDemand demand(
            NormalFlowComponent component,
            NormalFlowInput input
    ) {

        validateComponent(component);

        /*
         * IMPORTANT SEMANTIC RULE:
         *
         * baseDailyConsumptionLiters means:
         *
         *     liters / one component unit / day
         *
         * Example:
         *
         * Guest rooms:
         * quantity = 200
         * baseDailyConsumptionLiters = 180
         *
         * Base hotel room demand:
         *
         * 200 × 180 = 36,000 L/day
         *
         * before occupancy/efficiency/event adjustments.
         */
        double baseDemand =
                component.quantity()
                        * component.baseDailyConsumptionLiters();

        /*
         * HIGH_OCCUPANCY may override the normal hotel occupancy.
         */
        double effectiveOccupancy = eventOccupancy(
                component.type(),
                input.occupancyRate(),
                input.event()
        );

        /*
         * Occupancy-dependent assets do not drop completely to zero when
         * occupancy is zero.
         */
        double occupancyFactor = occupancyFactor(
                component,
                effectiveOccupancy
        );

        /*
         * Convert the normalized efficiency score into an understandable
         * water-consumption multiplier.
         */
        double efficiencyFactor =
                efficiencyFactor(component.efficiencyRating());

        /*
         * Small deterministic random operational variation.
         */
        double variabilityFactor =
                deterministicOperationalVariation(component, input.seed());

        /*
         * Scenario/event factor.
         *
         * Currently HEAT_WAVE affects:
         * - irrigation
         * - pool
         * - cooling tower
         */
        double eventFactor =
                heatWaveFactor(component.type(), input.event());

        /*
         * Archetype factor intentionally removed.
         *
         * The hotel archetype should already influence:
         * - generated components
         * - quantities
         * - occupancy
         * - efficiency
         * - component baselines
         *
         * Applying another archetype multiplier here would risk counting the
         * same archetype effect twice.
         */
        double dailyBaseline =
                baseDemand
                        * occupancyFactor
                        * efficiencyFactor
                        * variabilityFactor
                        * eventFactor;

        dailyBaseline = Math.max(0, dailyBaseline);

        /*
         * Turn daily demand into a realistic 24-hour profile.
         */
        double[] profile = normalizedProfile(component.type());

        List<Double> hourly = new ArrayList<>(24);

        for (int hour = 0; hour < 24; hour++) {

            double normalHourlyDemand =
                    dailyBaseline * profile[hour];

            double failureFlow =
                    additionalFailureFlow(
                            component,
                            hour,
                            input.event()
                    );

            hourly.add(
                    round(
                            normalHourlyDemand
                                    + failureFlow
                    )
            );
        }

        /*
         * Reconstruct displayed daily value from displayed hourly values.
         *
         * This avoids visible rounding mismatches in the frontend.
         */
        double roundedDaily = round(
                hourly.stream()
                        .mapToDouble(Double::doubleValue)
                        .sum()
        );

        return new ConsumerDemand(
                component,
                roundedDaily,
                List.copyOf(hourly)
        );
    }

    private void validateComponent(NormalFlowComponent component) {

        if (component == null) {
            throw new IllegalArgumentException(
                    "Simulation component must not be null"
            );
        }

        if (component.quantity() <= 0) {
            throw new IllegalArgumentException(
                    "Component quantity must be greater than zero: "
                            + component.name()
            );
        }

        if (component.baseDailyConsumptionLiters() < 0) {
            throw new IllegalArgumentException(
                    "Component baseDailyConsumptionLiters cannot be negative: "
                            + component.name()
            );
        }
    }

    // -------------------------------------------------------------------------
    // OCCUPANCY
    // -------------------------------------------------------------------------

    private double occupancyFactor(
            NormalFlowComponent component,
            double occupancyRate
    ) {

        if (!component.occupancyDependent()) {
            return 1.0;
        }

        double boundedOccupancy =
                clamp(occupancyRate, 0, 1);

        return FIXED_OCCUPANCY_DEMAND_SHARE
                + OCCUPANCY_DRIVEN_DEMAND_SHARE
                * boundedOccupancy;
    }

    private double eventOccupancy(
            ComponentType type,
            double baseline,
            NormalFlowEvent event
    ) {

        if (event == null
                || event.type() != FlowEventType.HIGH_OCCUPANCY
                || !occupancyAffected(type)) {

            return baseline;
        }

        double requestedOccupancy =
                event.highOccupancyRate() == null
                        ? 0.95
                        : event.highOccupancyRate();

        return clamp(
                Math.max(baseline, requestedOccupancy),
                0,
                1
        );
    }

    private boolean occupancyAffected(ComponentType type) {

        return type == ComponentType.GUEST_ROOMS
                || type == ComponentType.RESTAURANT
                || type == ComponentType.KITCHEN
                || type == ComponentType.LAUNDRY
                || type == ComponentType.SPA;
    }

    // -------------------------------------------------------------------------
    // EFFICIENCY
    // -------------------------------------------------------------------------

    private double efficiencyFactor(double efficiencyRating) {

        double bounded =
                clamp(efficiencyRating, 0, 1);

        /*
         * rating 0.0 -> 1.15
         * rating 0.5 -> 1.00
         * rating 1.0 -> 0.85
         */
        return INEFFICIENT_OPERATION_FACTOR
                - bounded * EFFICIENCY_FACTOR_RANGE;
    }

    // -------------------------------------------------------------------------
    // DETERMINISTIC VARIATION
    // -------------------------------------------------------------------------

    private double deterministicOperationalVariation(
            NormalFlowComponent component,
            long seed
    ) {

        Random random = new Random(
                seed
                        ^ ((long) component.type()
                        .name()
                        .hashCode() << 16)
                        ^ component.name().hashCode()
        );

        return RANDOM_FACTOR_MIN
                + random.nextDouble()
                * RANDOM_FACTOR_RANGE;
    }

    // -------------------------------------------------------------------------
    // HOURLY DEMAND PROFILES
    // -------------------------------------------------------------------------

    private double[] normalizedProfile(ComponentType type) {

        double[] values = new double[24];

        double total = 0;

        for (int hour = 0; hour < 24; hour++) {

            values[hour] =
                    hourlyWeight(type, hour);

            total += values[hour];
        }

        if (total <= 0) {
            throw new IllegalStateException(
                    "Hourly profile must contain positive demand weights for "
                            + type
            );
        }

        for (int hour = 0; hour < 24; hour++) {
            values[hour] /= total;
        }

        return values;
    }

    private double hourlyWeight(
            ComponentType type,
            int hour
    ) {

        return switch (type) {

            /*
             * Strong morning and evening guest-room usage.
             */
            case GUEST_ROOMS ->
                    peak(hour, 7, 2.3)
                            + peak(hour, 20, 2.0)
                            + 0.35;

            /*
             * Breakfast / lunch / dinner peaks.
             */
            case RESTAURANT, KITCHEN ->
                    peak(hour, 8, 1.5)
                            + peak(hour, 13, 2.0)
                            + peak(hour, 20, 2.2)
                            + 0.15;

            /*
             * Laundry mainly operates during daytime shifts.
             */
            case LAUNDRY ->
                    hour >= 7 && hour <= 17
                            ? 1.8
                            : 0.15;

            /*
             * Pool/spa usage concentrated during normal operating hours.
             */
            case POOL, SPA ->
                    hour >= 9 && hour <= 20
                            ? 1.4
                            : 0.25;

            /*
             * Strong early-morning irrigation and smaller evening cycle.
             */
            case IRRIGATION ->
                    peak(hour, 5, 4.0)
                            + peak(hour, 21, 1.5)
                            + 0.03;

            /*
             * Cooling demand follows hotter daytime periods.
             */
            case COOLING_TOWER ->
                    hour >= 10 && hour <= 18
                            ? 1.7
                            : 0.55;

            case OTHER -> 1.0;
        };
    }

    private double peak(
            int hour,
            int center,
            double height
    ) {

        int distance =
                Math.abs(hour - center);

        return distance == 0
                ? height
                : distance == 1
                ? height * 0.55
                : distance == 2
                ? height * 0.20
                : 0;
    }

    // -------------------------------------------------------------------------
    // HEAT WAVE
    // -------------------------------------------------------------------------

    private double heatWaveFactor(
            ComponentType type,
            NormalFlowEvent event
    ) {

        if (event == null
                || event.type() != FlowEventType.HEAT_WAVE) {

            return 1.0;
        }

        /*
         * These are simulation assumptions and should eventually become
         * configuration properties.
         */
        return switch (type) {

            case IRRIGATION -> 1.55;

            case POOL -> 1.25;

            case COOLING_TOWER -> 1.40;

            default -> 1.0;
        };
    }

    // -------------------------------------------------------------------------
    // LEAKS / PIPE BURSTS
    // -------------------------------------------------------------------------

    private double additionalFailureFlow(
            NormalFlowComponent component,
            int hour,
            NormalFlowEvent event
    ) {

        if (event == null
                || !active(event, hour)
                || !targets(component, event)) {

            return 0;
        }

        if (event.type() == FlowEventType.CONTINUOUS_LEAK) {

            return event.leakFlowLitersPerHour() == null
                    ? DEFAULT_CONTINUOUS_LEAK_LPH
                    : Math.max(
                    0,
                    event.leakFlowLitersPerHour()
            );
        }

        if (event.type() == FlowEventType.PIPE_BURST) {

            /*
             * If an explicit hydraulic/event rate was supplied, prefer it.
             */
            if (event.leakFlowLitersPerHour() != null) {

                return Math.max(
                        0,
                        event.leakFlowLitersPerHour()
                );
            }

            EventSeverity severity =
                    event.severity() == null
                            ? EventSeverity.MEDIUM
                            : event.severity();

            /*
             * Burst flow is based on event severity rather than normal
             * component consumption.
             *
             * That is conceptually cleaner: pipe rupture flow depends more on
             * the failure/network than on how much water the component normally
             * consumes.
             */
            return switch (severity) {

                case LOW ->
                        PIPE_BURST_LOW_LPH;

                case MEDIUM ->
                        PIPE_BURST_MEDIUM_LPH;

                case HIGH ->
                        PIPE_BURST_HIGH_LPH;
            };
        }

        return 0;
    }

    private boolean targets(
            NormalFlowComponent component,
            NormalFlowEvent event
    ) {

        if (event.targetComponent() == null) {
            return false;
        }

        String target =
                event.targetComponent().trim();

        return component.id().equalsIgnoreCase(target)
                || component.name().equalsIgnoreCase(target)
                || component.type()
                .name()
                .equalsIgnoreCase(target);
    }

    private boolean active(
            NormalFlowEvent event,
            int hour
    ) {

        if (event == null) {
            return false;
        }

        int start =
                Math.max(0, event.startHour());

        int duration =
                Math.max(0, event.durationHours());

        int end =
                Math.min(24, start + duration);

        return hour >= start && hour < end;
    }

    // -------------------------------------------------------------------------
    // SUPPLY INTERRUPTION / STORAGE
    // -------------------------------------------------------------------------

    private SupplyOutcome applySupplyInterruption(
            List<ConsumerDemand> original,
            NormalFlowEvent event
    ) {

        List<Double> normalTotal =
                hourlyTotals(original);

        /*
         * Under normal operation:
         *
         * source inflow == hotel demand.
         *
         * Storage is treated as hydraulically transparent/full.
         */
        if (event == null
                || event.type()
                != FlowEventType.WATER_SUPPLY_INTERRUPTION) {

            return new SupplyOutcome(
                    original,
                    normalTotal,
                    List.of(),
                    null
            );
        }

        double dailyDemand =
                original.stream()
                        .mapToDouble(ConsumerDemand::dailyLiters)
                        .sum();

        /*
         * Default storage = 50% of normal daily hotel demand.
         *
         * Example:
         *
         * hotel demand = 40,000 L/day
         * storage = 20,000 L
         */
        double capacity =
                event.storageCapacityLiters() == null
                        ? dailyDemand * 0.50
                        : Math.max(
                        0,
                        event.storageCapacityLiters()
                );

        /*
         * Calculate autonomy from event start using future demand profile.
         */
        double autonomy =
                calculateAutonomyHours(
                        normalTotal,
                        capacity,
                        event.startHour()
                );

        /*
         * Refill capacity.
         *
         * Example:
         *
         * 30,000 L tank / 6 hours
         * = 5,000 L/h maximum refill.
         */
        double refillCapacityPerHour =
                capacity <= 0
                        ? 0
                        : capacity
                        / DEFAULT_STORAGE_REFILL_HOURS;

        double remaining = capacity;

        List<Double> source =
                new ArrayList<>(24);

        List<Double> levels =
                new ArrayList<>(24);

        /*
         * Each component gets a new delivered-flow array.
         */
        List<ArrayList<Double>> adjusted =
                new ArrayList<>(original.size());

        for (int i = 0; i < original.size(); i++) {
            adjusted.add(new ArrayList<>(24));
        }

        for (int hour = 0; hour < 24; hour++) {

            double requested =
                    normalTotal.get(hour);

            double delivered;

            if (active(event, hour)) {

                /*
                 * Municipal source unavailable.
                 */
                source.add(0.0);

                /*
                 * Storage tries to satisfy demand.
                 */
                delivered =
                        Math.min(
                                requested,
                                remaining
                        );

                remaining =
                        Math.max(
                                0,
                                remaining - delivered
                        );

            } else {

                /*
                 * Municipal supply is available.
                 *
                 * It supplies current hotel demand and also gradually refills
                 * any storage deficit.
                 */
                delivered = requested;

                double storageDeficit =
                        Math.max(
                                0,
                                capacity - remaining
                        );

                double refill =
                        Math.min(
                                refillCapacityPerHour,
                                storageDeficit
                        );

                double municipalFlow =
                        requested + refill;

                source.add(
                        round(municipalFlow)
                );

                remaining =
                        Math.min(
                                capacity,
                                remaining + refill
                        );
            }

            /*
             * If storage cannot completely satisfy demand during outage,
             * scale consumer deliveries proportionally.
             *
             * This keeps:
             *
             * sum(component delivered flow)
             * ==
             * total delivered flow
             */
            double scale =
                    requested <= 0
                            ? 0
                            : delivered / requested;

            for (
                    int index = 0;
                    index < original.size();
                    index++
            ) {

                double componentDelivered =
                        original.get(index)
                                .hourlyLiters()
                                .get(hour)
                                * scale;

                adjusted
                        .get(index)
                        .add(
                                round(componentDelivered)
                        );
            }

            levels.add(
                    round(remaining)
            );
        }

        /*
         * Rebuild component demand objects using the water actually delivered.
         */
        List<ConsumerDemand> demands =
                new ArrayList<>(original.size());

        for (
                int index = 0;
                index < original.size();
                index++
        ) {

            List<Double> hourly =
                    List.copyOf(adjusted.get(index));

            double deliveredDaily =
                    round(
                            hourly.stream()
                                    .mapToDouble(Double::doubleValue)
                                    .sum()
                    );

            demands.add(
                    new ConsumerDemand(
                            original.get(index).component(),
                            deliveredDaily,
                            hourly
                    )
            );
        }

        return new SupplyOutcome(
                List.copyOf(demands),
                List.copyOf(source),
                List.copyOf(levels),
                round(autonomy)
        );
    }

    /**
     * Estimate how long a completely full storage tank could satisfy the
     * normal hotel demand starting from the interruption hour.
     *
     * We repeat the 24-hour demand profile if required.
     *
     * This is more meaningful than simply counting how many interruption
     * hours happened to be fully supplied.
     */
    private double calculateAutonomyHours(
            List<Double> hourlyDemand,
            double capacity,
            int startHour
    ) {

        if (capacity <= 0) {
            return 0;
        }

        double remaining = capacity;

        int normalizedStart =
                Math.floorMod(startHour, 24);

        /*
         * 72 hours is enough for this MVP and avoids an infinite result when
         * demand is extremely low.
         */
        for (int step = 0; step < 72; step++) {

            int hour =
                    Math.floorMod(
                            normalizedStart + step,
                            24
                    );

            double demand =
                    hourlyDemand.get(hour);

            if (demand <= 0) {
                continue;
            }

            /*
             * Tank cannot satisfy the entire next hour.
             *
             * Return fractional-hour autonomy.
             */
            if (remaining + EPSILON < demand) {

                return step
                        + remaining / demand;
            }

            remaining -= demand;
        }

        /*
         * Tank survived at least the full evaluation horizon.
         */
        return 72;
    }

    // -------------------------------------------------------------------------
    // NETWORK AGGREGATION
    // -------------------------------------------------------------------------

    private List<Double> hourlyTotals(
            List<ConsumerDemand> demands
    ) {

        List<Double> totals =
                new ArrayList<>(24);

        for (int hour = 0; hour < 24; hour++) {

            final int index = hour;

            double total =
                    demands.stream()
                            .mapToDouble(
                                    value ->
                                            value.hourlyLiters()
                                                    .get(index)
                            )
                            .sum();

            totals.add(
                    round(total)
            );
        }

        return List.copyOf(totals);
    }

    // -------------------------------------------------------------------------
    // NETWORK NODES
    // -------------------------------------------------------------------------

    private List<WaterNetworkNode> nodes(
            List<ConsumerDemand> demands,
            double total,
            List<Double> hourlyTotals,
            List<Double> sourceHourly,
            int currentHour
    ) {

        List<WaterNetworkNode> nodes =
                new ArrayList<>();

        double currentTotal =
                hourlyTotals.get(currentHour);

        double sourceDaily =
                round(
                        sourceHourly.stream()
                                .mapToDouble(Double::doubleValue)
                                .sum()
                );

        /*
         * Municipal source.
         */
        nodes.add(
                new WaterNetworkNode(
                        SOURCE,
                        WaterNetworkNode.NodeType.SOURCE,
                        "Municipal Supply",
                        null,
                        sourceHourly.get(currentHour),
                        sourceDaily,
                        100,
                        sourceHourly
                )
        );

        /*
         * Main storage/distribution node.
         *
         * Its throughput equals delivered hotel demand.
         */
        nodes.add(
                new WaterNetworkNode(
                        STORAGE,
                        WaterNetworkNode.NodeType.STORAGE,
                        "Main Storage Tank",
                        null,
                        currentTotal,
                        total,
                        100,
                        hourlyTotals
                )
        );

        /*
         * Consumer nodes.
         */
        for (ConsumerDemand demand : demands) {

            double share =
                    total == 0
                            ? 0
                            : round(
                            demand.dailyLiters()
                                    / total
                                    * 100
                    );

            nodes.add(
                    new WaterNetworkNode(
                            demand.component().id(),
                            WaterNetworkNode.NodeType.CONSUMER,
                            demand.component().name(),
                            demand.component().type(),
                            demand.hourlyLiters().get(currentHour),
                            demand.dailyLiters(),
                            share,
                            demand.hourlyLiters()
                    )
            );
        }

        return List.copyOf(nodes);
    }

    // -------------------------------------------------------------------------
    // NETWORK EDGES
    // -------------------------------------------------------------------------

    private List<WaterNetworkEdge> edges(
            List<ConsumerDemand> demands,
            List<Double> hourlyTotals,
            List<Double> sourceHourly,
            int currentHour
    ) {

        List<WaterNetworkEdge> edges =
                new ArrayList<>();

        /*
         * Source -> storage.
         *
         * During tank refill this can legitimately be greater than current
         * hotel demand because part of the incoming water restores storage.
         */
        edges.add(
                new WaterNetworkEdge(
                        SOURCE + "->" + STORAGE,
                        SOURCE,
                        STORAGE,
                        sourceHourly.get(currentHour),
                        round(
                                sourceHourly.stream()
                                        .mapToDouble(Double::doubleValue)
                                        .sum()
                        ),
                        sourceHourly
                )
        );

        /*
         * Storage/distribution -> consumers.
         */
        for (ConsumerDemand demand : demands) {

            edges.add(
                    new WaterNetworkEdge(
                            STORAGE
                                    + "->"
                                    + demand.component().id(),
                            STORAGE,
                            demand.component().id(),
                            demand.hourlyLiters()
                                    .get(currentHour),
                            demand.dailyLiters(),
                            demand.hourlyLiters()
                    )
            );
        }

        return List.copyOf(edges);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private double clamp(
            double value,
            double min,
            double max
    ) {

        return Math.max(
                min,
                Math.min(max, value)
        );
    }

    private double round(double value) {

        return Math.round(value * 100.0)
                / 100.0;
    }

    // -------------------------------------------------------------------------
    // INTERNAL TYPES
    // -------------------------------------------------------------------------

    private record ConsumerDemand(
            NormalFlowComponent component,
            double dailyLiters,
            List<Double> hourlyLiters
    ) {
    }

    private record SupplyOutcome(
            List<ConsumerDemand> demands,
            List<Double> sourceHourly,
            List<Double> storageLevels,
            Double remainingAutonomyHours
    ) {
    }
}