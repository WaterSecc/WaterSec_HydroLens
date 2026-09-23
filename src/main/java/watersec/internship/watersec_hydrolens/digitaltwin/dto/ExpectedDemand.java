package watersec.internship.watersec_hydrolens.digitaltwin.dto;

public record ExpectedDemand(
        double baselineLitersPerDay,
        double peakLitersPerHour,
        double minimumLitersPerDay,
        double maximumLitersPerDay,
        double confidence) {
}
