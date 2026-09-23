package watersec.internship.watersec_hydrolens.simulation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hydrolens.simulation")
public class SimulationProperties {
    private String version = "event-driven-1.0";
    private double guestsPerOccupiedRoom = 1.6;
    private double showersPerGuestDay = 0.9;
    private double showerLiters = 55.0;
    private double flushesPerGuestDay = 4.5;
    private double toiletFlushLiters = 6.0;
    private double handWashesPerGuestDay = 5.0;
    private double handWashLiters = 1.8;
    private double laundryCycleLiters = 120.0;
    private double restaurantMealLiters = 20.0;
    private double poolGuestLiters = 18.0;
    private double spaVisitLiters = 35.0;
    private double irrigationLitersPerUnit = 5.0;
    private double leakProbabilityPerDay = 0.08;
    private double leakLitersPerHour = 35.0;
    private int telemetryIntervalMinutes = 15;
    private double basePressureBar = 3.2;
    private double averageTemperatureC = 20.0;
    private double temperatureAmplitudeC = 10.0;
    private double hotWeatherThresholdC = 27.0;
    private double coldWeatherThresholdC = 12.0;
    private double hotWeatherDemandFactor = 1.18;
    private double coldWeatherDemandFactor = 0.85;
    private double summerDemandFactor = 1.10;
    private double winterDemandFactor = 0.96;
    public String getVersion() { return version; }
    public void setVersion(String value) { version = value; }

    public double getGuestsPerOccupiedRoom() { return guestsPerOccupiedRoom; }
    public void setGuestsPerOccupiedRoom(double value) { guestsPerOccupiedRoom = value; }
    public double getShowersPerGuestDay() { return showersPerGuestDay; }
    public void setShowersPerGuestDay(double value) { showersPerGuestDay = value; }
    public double getShowerLiters() { return showerLiters; }
    public void setShowerLiters(double value) { showerLiters = value; }
    public double getFlushesPerGuestDay() { return flushesPerGuestDay; }
    public void setFlushesPerGuestDay(double value) { flushesPerGuestDay = value; }
    public double getToiletFlushLiters() { return toiletFlushLiters; }
    public void setToiletFlushLiters(double value) { toiletFlushLiters = value; }
    public double getHandWashesPerGuestDay() { return handWashesPerGuestDay; }
    public void setHandWashesPerGuestDay(double value) { handWashesPerGuestDay = value; }
    public double getHandWashLiters() { return handWashLiters; }
    public void setHandWashLiters(double value) { handWashLiters = value; }
    public double getLaundryCycleLiters() { return laundryCycleLiters; }
    public void setLaundryCycleLiters(double value) { laundryCycleLiters = value; }
    public double getRestaurantMealLiters() { return restaurantMealLiters; }
    public void setRestaurantMealLiters(double value) { restaurantMealLiters = value; }
    public double getPoolGuestLiters() { return poolGuestLiters; }
    public void setPoolGuestLiters(double value) { poolGuestLiters = value; }
    public double getSpaVisitLiters() { return spaVisitLiters; }
    public void setSpaVisitLiters(double value) { spaVisitLiters = value; }
    public double getIrrigationLitersPerUnit() { return irrigationLitersPerUnit; }
    public void setIrrigationLitersPerUnit(double value) { irrigationLitersPerUnit = value; }
    public double getLeakProbabilityPerDay() { return leakProbabilityPerDay; }
    public void setLeakProbabilityPerDay(double value) { leakProbabilityPerDay = value; }
    public double getLeakLitersPerHour() { return leakLitersPerHour; }
    public void setLeakLitersPerHour(double value) { leakLitersPerHour = value; }
    public int getTelemetryIntervalMinutes() { return telemetryIntervalMinutes; }
    public void setTelemetryIntervalMinutes(int value) { telemetryIntervalMinutes = value; }
    public double getBasePressureBar() { return basePressureBar; }
    public void setBasePressureBar(double value) { basePressureBar = value; }
    public double getAverageTemperatureC() { return averageTemperatureC; }
    public void setAverageTemperatureC(double value) { averageTemperatureC = value; }
    public double getTemperatureAmplitudeC() { return temperatureAmplitudeC; }
    public void setTemperatureAmplitudeC(double value) { temperatureAmplitudeC = value; }
    public double getHotWeatherThresholdC() { return hotWeatherThresholdC; }
    public void setHotWeatherThresholdC(double value) { hotWeatherThresholdC = value; }
    public double getColdWeatherThresholdC() { return coldWeatherThresholdC; }
    public void setColdWeatherThresholdC(double value) { coldWeatherThresholdC = value; }
    public double getHotWeatherDemandFactor() { return hotWeatherDemandFactor; }
    public void setHotWeatherDemandFactor(double value) { hotWeatherDemandFactor = value; }
    public double getColdWeatherDemandFactor() { return coldWeatherDemandFactor; }
    public void setColdWeatherDemandFactor(double value) { coldWeatherDemandFactor = value; }
    public double getSummerDemandFactor() { return summerDemandFactor; }
    public void setSummerDemandFactor(double value) { summerDemandFactor = value; }
    public double getWinterDemandFactor() { return winterDemandFactor; }
    public void setWinterDemandFactor(double value) { winterDemandFactor = value; }

    public java.util.Map<String, Object> snapshot() {
        java.util.Map<String, Object> values = new java.util.LinkedHashMap<>();
        values.put("guestsPerOccupiedRoom", guestsPerOccupiedRoom);
        values.put("showersPerGuestDay", showersPerGuestDay);
        values.put("showerLiters", showerLiters);
        values.put("flushesPerGuestDay", flushesPerGuestDay);
        values.put("toiletFlushLiters", toiletFlushLiters);
        values.put("handWashesPerGuestDay", handWashesPerGuestDay);
        values.put("handWashLiters", handWashLiters);
        values.put("laundryCycleLiters", laundryCycleLiters);
        values.put("restaurantMealLiters", restaurantMealLiters);
        values.put("poolGuestLiters", poolGuestLiters);
        values.put("spaVisitLiters", spaVisitLiters);
        values.put("irrigationLitersPerUnit", irrigationLitersPerUnit);
        values.put("leakProbabilityPerDay", leakProbabilityPerDay);
        values.put("leakLitersPerHour", leakLitersPerHour);
        values.put("telemetryIntervalMinutes", telemetryIntervalMinutes);
        values.put("basePressureBar", basePressureBar);
        values.put("averageTemperatureC", averageTemperatureC);
        values.put("temperatureAmplitudeC", temperatureAmplitudeC);
        values.put("hotWeatherThresholdC", hotWeatherThresholdC);
        values.put("coldWeatherThresholdC", coldWeatherThresholdC);
        values.put("hotWeatherDemandFactor", hotWeatherDemandFactor);
        values.put("coldWeatherDemandFactor", coldWeatherDemandFactor);
        values.put("summerDemandFactor", summerDemandFactor);
        values.put("winterDemandFactor", winterDemandFactor);
        return java.util.Map.copyOf(values);
    }
}
